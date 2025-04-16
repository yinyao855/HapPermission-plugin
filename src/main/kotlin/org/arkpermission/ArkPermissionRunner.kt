package org.arkpermission

import java.io.*
import javax.swing.SwingUtilities

object ArkPermissionRunner {
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
                        ArkPermissionToolWindowFactory.outputArea?.append(line + "\n")
                    }
                }
            }.start()
        } catch (e: Exception) {
            SwingUtilities.invokeLater {
                ArkPermissionToolWindowFactory.outputArea?.append("Error running Node.js: ${e.message}\n")
            }
        }
    }
}