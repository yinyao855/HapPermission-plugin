package org.arkpermission

import java.io.*
import javax.swing.SwingUtilities

object PermissionCheckerRunner {
    fun runScript() {
        val scriptFile = File("node/hello.js").absolutePath
        println(scriptFile)
        try {
            val process = ProcessBuilder("node", scriptFile)
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            Thread {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    SwingUtilities.invokeLater {
                        PermissionCheckerToolWindowFactory.outputArea?.append(line + "\n")
                    }
                }
            }.start()
        } catch (e: Exception) {
            SwingUtilities.invokeLater {
                PermissionCheckerToolWindowFactory.outputArea?.append("Error running Node.js: ${e.message}\n")
            }
        }
    }
}