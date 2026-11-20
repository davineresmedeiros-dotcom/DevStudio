package dev.devstudio.app.project

data class Project(
    val name: String,
    val packageName: String,
    val createdAt: Long = System.currentTimeMillis()
)
