package org.arkpermission

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class PermissionCheckerToolWindowFactory : ToolWindowFactory, DumbAware {
    companion object {
        var outputArea: JTextArea? = null
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val textArea = JTextArea()
        textArea.isEditable = false
        outputArea = textArea

        val panel = JPanel(BorderLayout())
        panel.add(JScrollPane(textArea), BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(panel, "Permission Checker", false)
        toolWindow.contentManager.addContent(content)
    }
}