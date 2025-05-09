import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.jar.JarFile

object ResourceExtractor {
    private fun getPluginExtractDir(pluginId: String): Path {
        val descriptor = PluginManagerCore.getLoadedPlugins()
            .firstOrNull { it.pluginId.idString == pluginId }
            ?: throw IllegalStateException("Plugin $pluginId not loaded")
        val pluginDir = Path.of(
            PathManager.getPluginsPath(),
            pluginId,
            descriptor.version ?: "1.0.0",
            "lib"
        )
        return pluginDir
    }

    fun extractLibFolder(pluginId: String): Path {
        val extractDir = getPluginExtractDir(pluginId)
        if (Files.exists(extractDir)) {
            println("use cached js")
            return extractDir
        }

        val jarFile = getJarFile()
        println(jarFile.name)

        JarFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.startsWith("lib/") && !it.isDirectory }
                .forEach { entry ->
                    val targetPath = extractDir.resolve(entry.name.substringAfter("lib/"))
                    Files.createDirectories(targetPath.parent)
                    jar.getInputStream(entry).use { input ->
                        Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
        }
        return extractDir
    }

    private fun getJarFile(): File {
        val resourceUrl = javaClass.classLoader.getResource("META-INF/plugin.xml")

        return File(
            URLDecoder.decode(
                resourceUrl!!.path.substringAfter("file:").substringBefore("!"),
                StandardCharsets.UTF_8
            )
        )
    }
}