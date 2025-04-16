package org.arkpermission

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class ArkPermissionAction : AnAction("Run ArkPermission") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ArkPermission")
        toolWindow?.show()
        ArkPermissionToolWindowFactory.outputArea?.text = ""
        ArkPermissionRunner.runScript()
    }
}