package dev.kikugie.stonecutter.intellij.util

const val REFRESH_ACTIVE_TASK = "\"Refresh active project\""
const val RESET_ACTIVE_TASK = "\"Reset active project\""
const val SWITCH_TASK_TEMPLATE = "Set active project to"
fun switchTask(project: String) = "\"$SWITCH_TASK_TEMPLATE $project\""