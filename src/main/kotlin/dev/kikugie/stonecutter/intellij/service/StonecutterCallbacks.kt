package dev.kikugie.stonecutter.intellij.service

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDocument
import com.intellij.openapi.vfs.toNioPathOrNull
import dev.kikugie.stonecutter.intellij.action.VersionSelectorAction
import dev.kikugie.stonecutter.intellij.editor.StitcherFoldingBuilder.Constants.STITCHER_SCOPE
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode.AGGRESSIVE
import java.awt.event.FocusEvent

object StonecutterCallbacks {
    internal fun invokeAppLoad(settings: StonecutterSettings) {
        //? if >=2025 {
        /*val bus = ApplicationManager.getApplication().messageBus.connect(settings)
        @Suppress("UnstableApiUsage") bus.subscribe(
            org.jetbrains.plugins.gradle.service.syncAction.GradleSyncListener.TOPIC,
            dev.kikugie.stonecutter.intellij.service.gradle.GradleReloadListener
        )
        *///?}
        (EditorFactory.getInstance().eventMulticaster as? EditorEventMulticasterEx)
            ?.addFocusChangeListener(createFocusListener(), settings)
    }

    internal fun invokeProjectLoad(service: StonecutterService) {
        val bus = service.project.messageBus.connect(service)
        bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileListener(service))
    }

    internal fun invokeProjectReload(service: StonecutterService) {
        (ActionManager.getInstance().getAction("dev.kikugie.stonecutter.intellij.select_version") as VersionSelectorAction)
            .isAvailable = true

        if (StonecutterSettings.STATE.foldDisabledBlocks == AGGRESSIVE) runWheneverIntelliJWantsIt {
            FileEditorManager.getInstance(service.project).focusedEditor?.file
                ?.findDocument()
                ?.let(EditorFactory.getInstance()::getEditors)
                ?.forEach(::updateFolding)
        }
    }

    //<editor-fold desc="Implementations">
    /**
     * Update folding for blocks commented out by Stonecutter.
     * Can be disabled in code folding settings.
     */
    private fun createFocusListener() = object : FocusChangeListener {
        override fun focusGained(editor: Editor, event: FocusEvent) {
            if (StonecutterSettings.STATE.foldDisabledBlocks == AGGRESSIVE && !event.isTemporary)
                runWheneverIntelliJWantsIt { updateFolding(editor) }
        }
    }

    private fun updateFolding(editor: Editor) = editor.foldingModel.runBatchFoldingOperation {
        for (it in editor.foldingModel.allFoldRegions) if (it.group == STITCHER_SCOPE)
            it.isExpanded = false
    }

    /**
     * Disallow editing generated Stonecutter files.
     */
    private fun createFileListener(service: StonecutterService) = object : FileEditorManagerListener {
        override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
            if (StonecutterSettings.STATE.lockGeneratedFiles)
                runWheneverIntelliJWantsIt { updateFile(service, source, file) }
        }

        private fun updateFile(service: StonecutterService, source: FileEditorManager, file: VirtualFile) {
            val path = file.toNioPathOrNull() ?: return
            val root = service.lookup.all
                .find { path.startsWith(it.location.resolve("build")) }
                ?: return
            val isGenerated = path.startsWith(root.location.resolve("build/generated/stonecutter"))
            val isCache = path.startsWith(root.location.resolve("build/stonecutter-cache"))
            if (isGenerated || isCache) file.findDocument()?.setReadOnly(true)
        }
    }

    private fun runWheneverIntelliJWantsIt(action: () -> Unit) {
        val application = ApplicationManager.getApplication()
        application.invokeLater { application.runWriteAction(action) }
    }
    //</editor-fold>
}