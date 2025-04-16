package org.arkpermission

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class PermissionCheckerAction : AnAction("Run Permission Checker") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Permission Checker")
        toolWindow?.show()
        PermissionCheckerToolWindowFactory.outputArea?.text = ""
        PermissionCheckerRunner.runScript()
    }
}