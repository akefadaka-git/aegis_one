package org.akefadaka.aegis
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
/** Minimal shell; no device controls are enabled by this activity. */
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(TextView(this).apply {
   text="AEGIS ONE\n\nLocal protection foundation\nSecurity posture: UNKNOWN\nNo device controls are active."
   textSize=18f; setPadding(32,48,32,32)
  })
 }
}
