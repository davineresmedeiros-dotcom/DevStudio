package dev.devstudio.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import dev.devstudio.app.project.ProjectManager

class ProjectEditorActivity : Activity() {

    private val bg = Color.rgb(16, 24, 39)
    private val panel = Color.rgb(35, 48, 67)
    private val green = Color.rgb(101, 230, 165)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 192)

    private lateinit var manager: ProjectManager
    private lateinit var projectName: String
    private lateinit var packageName: String
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = ProjectManager(this)
        packageName = intent.getStringExtra("packageName") ?: run {
            finish()
            return
        }

        val project = manager.getAll()
            .firstOrNull { it.packageName == packageName } ?: run {
            finish()
            return
        }

        projectName = project.name

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(18, 24, 18, 18)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "‹"
            textSize = 25f
            isAllCaps = false
            setTextColor(green)
            setBackgroundColor(bg)
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = projectName
            textSize = 21f
            setTextColor(white)
            setPadding(12, 0, 0, 0)
        }

        header.addView(back)
        header.addView(title)
        root.addView(header)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 18, 0, 18)
        }

        tabs.addView(tabButton("Design") { showDesign() })
        tabs.addView(tabButton("Logic") { showLogic() })
        tabs.addView(tabButton("Config") { showSettings() })
        root.addView(tabs)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        showDesign()
    }

    private fun tabButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(white)
            setBackgroundColor(panel)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, 52, 1f).apply {
                marginEnd = 5
            }
        }
    }

    private fun heading(text: String) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = 21f
            setTextColor(green)
            setPadding(0, 12, 0, 18)
        })
    }

    private fun item(text: String, description: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
            setPadding(16, 15, 16, 15)
        }

        box.addView(TextView(this).apply {
            this.text = text
            textSize = 17f
            setTextColor(white)
        })

        box.addView(TextView(this).apply {
            this.text = description
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 5, 0, 0)
        })

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 12
        }

        content.addView(box, params)
    }

    private fun showDesign() {
        content.removeAllViews()
        heading("Design")
        item("Visual Editor", "Aqui será possível montar a interface arrastando componentes.")
        item("Componentes", "Botões, textos, imagens, campos e outros elementos.")
        item("Preview", "Pré-visualização da tela do aplicativo.")
    }

    private fun showLogic() {
        content.removeAllViews()
        heading("Logic")
        item("Blocos", "Eventos, condições, variáveis, funções e operações.")
        item("Java2Block", "Escreva código Java e converta para blocos dentro do Logic.")
        item("Custom Blocks", "Seus blocos personalizados ficarão disponíveis aqui.")
    }

    private fun showSettings() {
        content.removeAllViews()
        heading("Configurações")
        item("Nome", projectName)
        item("Package name", packageName)
        item("Formato", ".dvs")
        item("Diretório", "Armazenado em .Devstudio/projects/")
    }
}
