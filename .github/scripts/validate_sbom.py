#!/usr/bin/env python3
"""Validate complete project-level and aggregate CycloneDX dependency evidence."""
import json
import sys
from collections import deque
from pathlib import Path

DIRECT_BOMS = {
    "app": Path("app/build/reports/cyclonedx-direct/bom.json"),
    "security-state": Path("core/security-state/build/reports/cyclonedx-direct/bom.json"),
    "policy": Path("core/policy/build/reports/cyclonedx-direct/bom.json"),
    "offline-policy": Path("core/offline-policy/build/reports/cyclonedx-direct/bom.json"),
    "audit": Path("core/audit/build/reports/cyclonedx-direct/bom.json"),
    "vault": Path("core/vault/build/reports/cyclonedx-direct/bom.json"),
    "android-capabilities": Path("platform/android-capabilities/build/reports/cyclonedx-direct/bom.json"),
}
AGGREGATE = Path("build/reports/cyclonedx/bom.json")
GROUP = "org.akefadaka.aegis"
PROJECT_REFS = {
    name: f"pkg:maven/{GROUP}/{name}@0.1.0?project_path=%3A{path}"
    for name, path in {
        "app": "app",
        "security-state": "core%3Asecurity-state",
        "policy": "core%3Apolicy",
        "offline-policy": "core%3Aoffline-policy",
        "audit": "core%3Aaudit",
        "vault": "core%3Avault",
        "android-capabilities": "platform%3Aandroid-capabilities",
    }.items()
}
EXPECTED_EDGES = {
    "app": {"security-state", "policy", "offline-policy", "audit", "android-capabilities"},
    "policy": {"security-state"},
    "offline-policy": {"security-state"},
    "android-capabilities": {"security-state"},
}


def load_bom(path: Path) -> dict:
    if not path.is_file():
        raise ValueError(f"missing SBOM: {path}")
    try:
        bom = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError(f"unreadable SBOM {path}: {exc}") from exc
    if bom.get("bomFormat") != "CycloneDX" or not bom.get("specVersion") or not bom.get("serialNumber"):
        raise ValueError(f"{path}: invalid CycloneDX document metadata")
    return bom


def graph_for(path: Path, bom: dict):
    root_ref = ((bom.get("metadata") or {}).get("component") or {}).get("bom-ref")
    if not root_ref:
        raise ValueError(f"{path}: metadata.component.bom-ref is missing")
    components = bom.get("components") or []
    component_refs = {item.get("bom-ref") for item in components if item.get("bom-ref")}
    known_refs = component_refs | {root_ref}
    graph = {}
    for node in bom.get("dependencies") or []:
        ref = node.get("ref")
        if not ref:
            raise ValueError(f"{path}: dependency node has no ref")
        if ref not in known_refs:
            raise ValueError(f"{path}: dependency node ref is absent from metadata/components: {ref}")
        children = set(node.get("dependsOn") or [])
        unknown = children - known_refs
        if unknown:
            raise ValueError(f"{path}: unresolved dependency targets from {ref}: {sorted(unknown)}")
        graph[ref] = children
    return root_ref, component_refs, graph


def reachable_from(root_ref, graph):
    seen, queue = set(), deque([root_ref])
    while queue:
        ref = queue.popleft()
        if ref in seen:
            continue
        seen.add(ref)
        queue.extend(graph.get(ref, set()) - seen)
    return seen


def validate_direct(name: str, path: Path, bom: dict):
    root_ref, component_refs, graph = graph_for(path, bom)
    if root_ref not in graph:
        if name == "vault" and not component_refs and not graph:
            return root_ref, component_refs, graph
        raise ValueError(f"{path}: project metadata root is absent from dependency graph")
    if not graph[root_ref]:
        raise ValueError(f"{path}: project root has no dependency edges")
    seen = reachable_from(root_ref, graph)
    unreachable = component_refs - seen
    if unreachable:
        raise ValueError(f"{path}: disconnected components: {sorted(unreachable)}")

    for dep_name in EXPECTED_EDGES.get(name, set()):
        expected = PROJECT_REFS[dep_name]
        if expected not in graph[root_ref]:
            raise ValueError(f"{path}: expected direct project dependency {dep_name} is absent from root edges")
    return root_ref, component_refs, graph


def main() -> int:
    try:
        direct_results = {}
        for name, path in DIRECT_BOMS.items():
            bom = load_bom(path)
            result = validate_direct(name, path, bom)
            direct_results[name] = result
            _, components, graph = result
            print(f"PASS {path}: {len(components)} components; {sum(map(len, graph.values()))} edges")

        aggregate = load_bom(AGGREGATE)
        root_ref, component_refs, graph = graph_for(AGGREGATE, aggregate)
        if not graph:
            raise ValueError(f"{AGGREGATE}: dependency graph is empty")
        # The aggregate metadata root represents the Gradle build root, which has no
        # source dependencies of its own. CycloneDX may therefore omit that root from
        # the dependency graph. Every actual project/library component must still be
        # represented as a graph node or edge.
        represented = set(graph)
        for children in graph.values():
            represented.update(children)
        missing = component_refs - represented
        if missing:
            raise ValueError(f"{AGGREGATE}: components missing from dependency graph: {sorted(missing)}")

        for name, expected_ref in PROJECT_REFS.items():
            if expected_ref not in component_refs:
                raise ValueError(f"{AGGREGATE}: missing expected project component {name}")
            if expected_ref not in graph:
                raise ValueError(f"{AGGREGATE}: project dependency node missing for {name}")

        total_edges = sum(map(len, graph.values()))
        print(
            f"PASS {AGGREGATE}: {len(component_refs)} components; {total_edges} edges; "
            f"6/6 project Direct SBOMs validated"
        )
        return 0
    except ValueError as exc:
        print(f"SBOM VALIDATION FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
