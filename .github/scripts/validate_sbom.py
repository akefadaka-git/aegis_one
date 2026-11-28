#!/usr/bin/env python3
"""Validate the AEGIS ONE aggregate and per-project CycloneDX SBOM evidence."""
import json
import sys
from collections import deque
from pathlib import Path

DIRECT_BOMS = [
    Path("build/reports/cyclonedx-direct/bom.json"),
    Path("app/build/reports/cyclonedx-direct/bom.json"),
    Path("core/security-state/build/reports/cyclonedx-direct/bom.json"),
    Path("core/policy/build/reports/cyclonedx-direct/bom.json"),
    Path("core/offline-policy/build/reports/cyclonedx-direct/bom.json"),
    Path("core/audit/build/reports/cyclonedx-direct/bom.json"),
    Path("platform/android-capabilities/build/reports/cyclonedx-direct/bom.json"),
]
AGGREGATE = Path("build/reports/cyclonedx/bom.json")


def load_bom(path: Path) -> dict:
    if not path.is_file():
        raise ValueError(f"missing SBOM: {path}")
    try:
        bom = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError(f"unreadable SBOM {path}: {exc}") from exc
    if bom.get("bomFormat") != "CycloneDX":
        raise ValueError(f"{path}: bomFormat is not CycloneDX")
    if not bom.get("specVersion") or not bom.get("serialNumber"):
        raise ValueError(f"{path}: missing CycloneDX schema version or serial number")
    return bom


def validate_references(path: Path, bom: dict, require_reachable: bool) -> None:
    metadata_component = (bom.get("metadata") or {}).get("component") or {}
    root_ref = metadata_component.get("bom-ref")
    if not root_ref:
        raise ValueError(f"{path}: metadata.component.bom-ref is missing")

    components = bom.get("components") or []
    component_refs = {item.get("bom-ref") for item in components if item.get("bom-ref")}
    known_refs = component_refs | {root_ref}
    dependency_nodes = bom.get("dependencies") or []
    graph = {}
    for node in dependency_nodes:
        ref = node.get("ref")
        if not ref:
            raise ValueError(f"{path}: dependency node has no ref")
        if ref not in known_refs:
            raise ValueError(f"{path}: dependency ref is absent from metadata/components: {ref}")
        children = set(node.get("dependsOn") or [])
        unknown = children - known_refs
        if unknown:
            raise ValueError(f"{path}: unresolved dependency references from {ref}: {sorted(unknown)}")
        graph[ref] = children

    if root_ref not in graph:
        raise ValueError(f"{path}: metadata root is absent from dependency graph")
    if require_reachable:
        seen = set()
        queue = deque([root_ref])
        while queue:
            ref = queue.popleft()
            if ref in seen:
                continue
            seen.add(ref)
            queue.extend(graph.get(ref, set()) - seen)
        unreachable = component_refs - seen
        if unreachable:
            raise ValueError(
                f"{path}: {len(unreachable)} component(s) are disconnected from metadata root: "
                f"{sorted(unreachable)}"
            )


def main() -> int:
    try:
        aggregate = load_bom(AGGREGATE)
        validate_references(AGGREGATE, aggregate, require_reachable=False)
        total_components = len(aggregate.get("components") or [])
        total_edges = sum(len(node.get("dependsOn") or []) for node in aggregate.get("dependencies") or [])
        if not total_components or not total_edges:
            raise ValueError(f"{AGGREGATE}: aggregate contains no components or dependency edges")

        for path in DIRECT_BOMS:
            bom = load_bom(path)
            validate_references(path, bom, require_reachable=True)
            print(
                f"PASS {path}: {len(bom.get('components') or [])} components; "
                f"{sum(len(n.get('dependsOn') or []) for n in bom.get('dependencies') or [])} edges"
            )

        print(
            f"PASS {AGGREGATE}: {total_components} aggregate components; "
            f"{total_edges} dependency edges; 7/7 Direct SBOMs validated"
        )
        return 0
    except ValueError as exc:
        print(f"SBOM VALIDATION FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
