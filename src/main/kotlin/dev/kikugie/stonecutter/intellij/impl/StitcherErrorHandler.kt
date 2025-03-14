package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.util.NlsActions

class StitcherErrorHandler : ErrorReportSubmitter() {
    override fun getReportActionText(): @NlsActions.ActionText String {
        TODO("Not yet implemented") // To be also reported on GitHub
    }
}