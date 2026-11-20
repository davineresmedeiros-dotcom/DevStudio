package dev.devstudio.app

import android.app.Activity
import android.content.Intent
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import dev.devstudio.app.project.Project
import dev.devstudio.app.project.ProjectManager

class MainActivity : Activity() {

    private val bg = Color.rgb(16, 24, 39)
    private val green = Color.rgb(101, 230, 165)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 192)

    private lateinit var manager: ProjectManager
    private lateinit var projectList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = ProjectManager(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(24, 30, 24, 20)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val logo = TextView(this).apply {
            text = ">_"
            textSize = 42f
            setTextColor(green)
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "DevStudio"
            textSize = 30f
            setTextColor(white)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "BUILD YOUR OWN APPS"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }

        content.addView(logo)
        content.addView(title)
        content.addView(subtitle)

        val newButton = createButton("＋  Novo projeto")
        newButton.setOnClickListener { showCreateDialog() }
        content.addView(newButton)

        val heading = TextView(this).apply {
            text = "MEUS PROJETOS"
            textSize = 13f
            setTextColor(green)
            setPadding(0, 32, 0, 14)
        }
        content.addView(heading)

        projectList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(projectList)

        val modes = TextView(this).apply {
            text = "AMBIENTES DE DESENVOLVIMENTO"
            textSize = 12f
            setTextColor(green)
            setPadding(0, 32, 0, 14)
        }
        content.addView(modes)

        val block = createButton("▦  Block Editor")
        block.setOnClickListener { showMessage("O editor de blocos será desenvolvido em seguida.") }
        content.addView(block)

        val androidStudio = createButton("</>  Android Studio")
        androidStudio.setOnClickListener { showMessage("O editor Android Studio será desenvolvido em seguida.") }
        content.addView(androidStudio)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)

        refreshProjects()
    }

    private fun createButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 16f
            setTextColor(white)
            setBackgroundColor(Color.rgb(35, 48, 67))
            isAllCaps = false
            setPadding(12, 14, 12, 14)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
                topMargin = 8
            }
        }
    }

    private fun showCreateDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(45, 15, 45, 5)
        }

        val nameInput = EditText(this).apply {
            hint = "Nome do projeto"
            singleLine = true
        }

        val packageInput = EditText(this).apply {
            hint = "com.example.meuapp"
            singleLine = true
        }

        layout.addView(nameInput)
        layout.addView(packageInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Novo projeto")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Criar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()
                val packageName = packageInput.text.toString().trim()

                if (name.isBlank()) {
                    nameInput.error = "Informe um nome"
                    return@setOnClickListener
                }

                if (!manager.create(name, packageName)) {
                    packageInput.error = "Package inválido ou já existente"
                    return@setOnClickListener
                }

                dialog.dismiss()
                refreshProjects()
                showMessage("Projeto criado!")
            }
        }

        dialog.show()
    }

    private fun refreshProjects() {
        projectList.removeAllViews()

        val projects = manager.getAll()

        if (projects.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Nenhum projeto ainda. Crie seu primeiro aplicativo!"
                textSize = 14f
                setTextColor(muted)
                setPadding(0, 12, 0, 20)
            }
            projectList.addView(empty)
            return
        }

        projects.forEach { project ->
            addProjectCard(project)
        }
    }

    private fun addProjectCard(project: Project) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(Color.rgb(35, 48, 67))
        }

        val name = TextView(this).apply {
            text = project.name
            textSize = 18f
            setTextColor(white)
        }

        val packageName = TextView(this).apply {
            text = project.packageName
            textSize = 12f
            setTextColor(muted)
        }

        card.addView(name)
        card.addView(packageName)

        card.setOnClickListener {
            startActivity(Intent(this, ProjectEditorActivity::class.java).putExtra("packageName", project.packageName))
        }

        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Excluir projeto")
                .setMessage("Deseja excluir ${project.name}?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ ->
                    manager.delete(project.packageName)
                    refreshProjects()
                }
                .show()
            true
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 12
        }

        projectList.addView(card, params)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
