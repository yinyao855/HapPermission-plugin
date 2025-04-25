package org.happermission

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
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

        // Tab 2 - Output Display
        val outputArea = JTextArea()
        outputArea.isEditable = false
        val outputScrollPane = JScrollPane(outputArea)
        val panel2 = JPanel(BorderLayout())
        panel2.add(outputScrollPane, BorderLayout.CENTER)
        val content2 = contentFactory.createContent(panel2, "日志", false)

        toolWindow.component.putClientProperty("outputArea", outputArea)

        // Tab 1 - Configuration with Run Button
        val panel1 = createConfigPanel(toolWindow)
        val content1 = contentFactory.createContent(panel1, "配置", false)

        // Tab 3 - CSV Display (same as before)
        val panel3 = createCsvPanel("PerAPI/14.csv") // Update the CSV path
        val content3 = contentFactory.createContent(panel3, "分析结果", false)

        content1.isCloseable = false
        content2.isCloseable = false
        content3.isCloseable = false
        toolWindow.contentManager.addContent(content1)
        toolWindow.contentManager.addContent(content2)
        toolWindow.contentManager.addContent(content3)
    }

    private fun createConfigPanel(toolWindow: ToolWindow): JPanel {
        val panel = JPanel(BorderLayout())

        // Create a panel with a button
        val runButton = JButton("Run Command")

        val outputArea = toolWindow.component.getClientProperty("outputArea") as JTextArea

        // Add action listener to the button
        runButton.addActionListener { e: ActionEvent? -> runCommand(outputArea) }

        // Layout setup
        panel.add(runButton, BorderLayout.NORTH)

        return panel
    }

    private fun runCommand(outputArea: JTextArea) {
        // Define the command you want to run (example: "ping google.com" for testing)
        val command = "shuf -i 1-100 -n 1" // Replace with your actual command
        try {
            // Execute the command
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            val output = StringBuilder()

            // Read the output of the command
            while ((reader.readLine().also { line = it }) != null) {
                output.append(line).append("\n")
            }

            // Set the output to the JTextArea
            outputArea.text = output.toString()

            // Wait for the command to finish
            process.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
            outputArea.text = "Error running command: " + e.message
        } catch (e: InterruptedException) {
            e.printStackTrace()
            outputArea.text = "Error running command: " + e.message
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createCsvPanel(filePath: String): JPanel {
        val panel = JPanel(BorderLayout())

        // Table model and JTable to display CSV
        val model = DefaultTableModel()
        val table = JTable(model)
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF

        // Load CSV into table
        loadCsvData(filePath, model)

        val scrollPane = JScrollPane(table)

        val columnCount = table.columnCount
        val columnModel = table.columnModel
        for (i in 0..<columnCount) {
            columnModel.getColumn(i).preferredWidth = 200
        }

        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
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