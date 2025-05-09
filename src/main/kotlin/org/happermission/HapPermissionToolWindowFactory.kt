package org.happermission

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.swing.*
import javax.swing.table.DefaultTableModel


class HapPermissionToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()

        // Tab 3 - Display
        val panel3 = createResultPanel(project, toolWindow)
        val content3 = contentFactory.createContent(panel3, "分析结果", false)

        toolWindow.component.putClientProperty("result", content3)

        // Tab 2 - Output Display
        val outputArea = JTextArea()
        outputArea.isEditable = false
        val outputScrollPane = JBScrollPane(outputArea)
        val panel2 = JPanel(BorderLayout())
        panel2.add(outputScrollPane, BorderLayout.CENTER)
        val content2 = contentFactory.createContent(panel2, "日志", false)

        toolWindow.component.putClientProperty("outputArea", outputArea)

        // Tab 1 - Configuration with Run Button
        val panel1 = createCommandArgsPanel(project, toolWindow, content2)
        val content1 = contentFactory.createContent(panel1, "配置", false)

        content1.isCloseable = false
        content2.isCloseable = false
        content3.isCloseable = false
        toolWindow.contentManager.addContent(content1)
        toolWindow.contentManager.addContent(content2)
        toolWindow.contentManager.addContent(content3)
    }


    private fun loadCsvData(filePath: String, model: DefaultTableModel) {
        // 从插件的资源目录加载 CSV 文件
        val inputStream = javaClass.classLoader.getResourceAsStream(filePath)

        if (inputStream == null) {
            println("CSV file not found in resources.")
            // 这里你可以弹出一个错误提示框，或者其他方式提示用户文件未找到
            return
        }

        try {
            BufferedReader(InputStreamReader(inputStream)).use { br ->
                var line: String?
                var firstLine = true
                while (true) {
                    line = br.readLine()
                    if (line == null) break

                    val row = line.split(",").map { it.trim() }.toTypedArray()

                    if (firstLine) {
                        // Add column headers from the first line
                        for (header in row) {
                            model.addColumn(header)
                        }
                        firstLine = false
                    } else {
                        // Add data rows
                        model.addRow(row)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 处理错误，显示错误消息等
        }
    }
}