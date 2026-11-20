package dev.devstudio.app.project

import android.content.Context
import org.json.JSONObject
import java.io.File

class ProjectManager(context: Context) {

    private val root = File(context.filesDir, ".Devstudio")
    private val directory = File(root, "projects")

    init {
        listOf(
            directory,
            File(root, "custom_blocks"),
            File(root, "templates"),
            File(root, "assets"),
            File(root, "exports"),
            File(root, "backups")
        ).forEach { it.mkdirs() }
    }

    private fun slug(name: String): String {
        return name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_-]+"), "_")
            .trim('_', '-')
            .ifBlank { "projeto" }
    }

    fun create(name: String, packageName: String): Boolean {
        if (name.isBlank() || packageName.isBlank()) return false

        if (!packageName.matches(
            Regex("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+")
        )) return false

        if (getAll().any { it.packageName == packageName }) return false

        val projectDir = File(directory, "${slug(name)}.dvs")
        if (projectDir.exists()) return false

        return try {
            projectDir.mkdirs()

            listOf("assets", "screens", "logic", "resources")
                .forEach { File(projectDir, it).mkdirs() }

            val project = Project(name, packageName)

            val json = JSONObject().apply {
                put("name", project.name)
                put("packageName", project.packageName)
                put("createdAt", project.createdAt)
                put("format", "dvs")
                put("formatVersion", 1)
            }

            File(projectDir, "project.json").writeText(json.toString(4))
            true
        } catch (_: Exception) {
            projectDir.deleteRecursively()
            false
        }
    }

    fun getAll(): List<Project> {
        return directory.listFiles()
            ?.filter { it.isDirectory && it.extension == "dvs" }
            ?.mapNotNull {
                try {
                    val json = JSONObject(File(it, "project.json").readText())
                    Project(
                        json.getString("name"),
                        json.getString("packageName"),
                        json.getLong("createdAt")
                    )
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
    }

    fun getProjectDirectory(packageName: String): File? {
        return directory.listFiles()
            ?.firstOrNull { folder ->
                folder.isDirectory &&
                folder.extension == "dvs" &&
                try {
                    JSONObject(File(folder, "project.json").readText())
                        .getString("packageName") == packageName
                } catch (_: Exception) {
                    false
                }
            }
    }

    fun delete(packageName: String): Boolean {
        val folder = getProjectDirectory(packageName) ?: return false
        return folder.deleteRecursively()
    }

    fun getRootDirectory(): File = root

    fun getCustomBlocksDirectory(): File =
        File(root, "custom_blocks")
}
