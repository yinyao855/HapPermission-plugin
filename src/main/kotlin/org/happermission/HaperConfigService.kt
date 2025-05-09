package org.happermission

import com.google.gson.GsonBuilder
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.table.JBTable
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import javax.swing.JTextArea
import javax.swing.SwingWorker
import javax.swing.table.DefaultTableModel
import kotlin.io.path.absolutePathString

object HaperConfigService {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val nodePath = findNodePath()

    fun getSavePath(project: Project): File {
        val configFile = project.basePath + "/haper-config.json"
        return File(configFile)
    }

    fun load(project: Project): HaperConfig {
        val config = getSavePath(project)
        val res = if (config.exists()) {
            gson.fromJson(config.readText(), HaperConfig::class.java)
        } else {
            HaperConfig()
        }
        res.setDefaultValues(project)
        return res
    }

    fun save(args: HaperConfig, project: Project) {
        val file = getSavePath(project)
        file.parentFile.mkdirs()
        println(args)
        file.writeText(gson.toJson(args))
    }

    fun findNodePath(): String? {
        val command = if (SystemInfo.isWindows) listOf("cmd.exe", "/c", "where", "node")
        else listOf("which", "node")
        println(command)
        return ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readLine()
            ?.let { path ->
                println("find: $path")
                if (SystemInfo.isWindows) {
                    path.split(System.lineSeparator())
                        .firstOrNull { File(it).exists() }
                } else {
                    path.takeIf { File(it).exists() }
                }
            } ?: "/opt/homebrew/bin/node"
    }

    fun runCommand(script: String, basePath: Path, toolWindow: ToolWindow) {
        println(nodePath)
        val outputArea = toolWindow.component.getClientProperty("outputArea") as JTextArea
        val result = toolWindow.component.getClientProperty("result") as Content
        val model = toolWindow.component.getClientProperty("model") as DefaultTableModel
        val table = toolWindow.component.getClientProperty("table") as JBTable
        outputArea.text = ""
        val configPath = basePath.resolve("haper-config.json").absolutePathString()
        val command = listOf(nodePath, script, "--config", configPath)

        val worker = object : SwingWorker<Void, String>() {
            override fun doInBackground(): Void? {
                try {
                    val process = ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start()

                    val stdInput = BufferedReader(InputStreamReader(process.inputStream))
                    var s: String?
                    while (stdInput.readLine().also { s = it } != null) {
                        publish(s)
                    }

                    val stdError = BufferedReader(InputStreamReader(process.errorStream))
                    while (stdError.readLine().also { s = it } != null) {
                        publish(s)
                    }

                    val exitCode = process.waitFor()
                    publish("Process exited with code $exitCode")

                    if (exitCode == 0) {
                        invokeLater {
                            val excelFile = File(basePath.resolve("haper_report.xlsx").absolutePathString())
                            loadExcelFile(excelFile, model, table)
                            toolWindow.contentManager.setSelectedContent(result)
                        }
                    }

                } catch (e: Exception) {
                    publish("Error: ${e.message}")
                    e.printStackTrace()
                }
                return null
            }

            override fun process(chunks: List<String>) {
                for (line in chunks) {
                    outputArea.append("$line\n")
                }
            }
        }

        worker.execute()
    }
}