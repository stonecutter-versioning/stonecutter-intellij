package dev.kikugie.stonecutter.intellij

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PluginAssets {
    fun icon(path: String) = IconLoader.getIcon("/assets/icons/$path", javaClass)

    val SWITCH_TO_VERSION: Icon = icon("switch.png")
    val VERSION_SELECTOR: Icon = icon("versions.svg")
    val VERSION_ENTRY: Icon = icon("version.svg")
    val VERSION_VCS: Icon = icon("version_vcs.svg")
    val STONECUTTER: Icon = icon("stonecutter.svg")
}