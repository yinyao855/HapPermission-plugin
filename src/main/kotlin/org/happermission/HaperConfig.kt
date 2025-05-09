package org.happermission

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File

data class HaperConfig(
    var appName: String = "",
    var appDir: String = "",
    var sdk: String = "",
    var output: String = "",
    var format: Format = Format.json,
    var scanTest: Boolean = false,
    var debug: Boolean = false,
    var noRepeat: Boolean = false,
) {
    fun setDefaultValues(project: Project) {
        if (appName.isEmpty()) {
            appName = project.name
        }
        if (appDir.isEmpty()) {
            appDir = project.basePath ?: ""
        }
        if (sdk.isEmpty()) {
            sdk = getDefaultSdkPath()
        }
        if (output.isEmpty()) {
            output = project.basePath ?: ""
        }
    }

    fun getDefaultSdkPath(): String {
        val path = when {
            SystemInfo.isWindows -> "AppData/Local/OpenHarmony/Sdk"
            SystemInfo.isMac -> "Library/OpenHarmony/Sdk"
            else -> "OpenHarmony/Sdk"
        }
        val defaultPath = File(System.getProperty("user.home"), path)
        return if (defaultPath.exists()) defaultPath.absolutePath else ""
    }
}

enum class Format {
    excel,
    csv,
    json
}