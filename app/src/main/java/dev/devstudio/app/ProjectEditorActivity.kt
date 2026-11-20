package dev.devstudio.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.*
import dev.devstudio.app.project.ProjectManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectEditorActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var preview: LinearLayout
    private lateinit var projectFolder: File

    private var projectName = "Meu projeto"
    private var packageName = ""

    private val components = JSONArray()
    private var selectedIndex = -1
    private val green = Color.rgb(101, 230, 165)
    private val dark = Color.rgb(16, 24, 39)
    private val panel = Color.rgb(28, 39, 56)

    private val componentTypes = listOf(
        "Texto", "Botão", "Imagem", "Campo de texto",
        "Checkbox", "Radio Button", "Switch",
        "Container", "Divisor", "Espaçador"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        packageName = intent.getStringExtra("packageName") ?: ""

        val manager = ProjectManager(this)
        projectFolder = manager.getProjectDirectory(packageName)
            ?: run {
                finish()
                return
            }

        val projectFile = File(projectFolder, "project.json")
        if (projectFile.exists()) {
            projectName = JSONObject(projectFile.readText())
                .optString("name", projectName)
        }

        loadLayout()
        buildInterface()
        showDesign()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 32f
            setTextColor(green)
            setPadding(dp(8), 0, dp(18), 0)
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = projectName
            textSize = 19f
            setTextColor(Color.WHITE)
        }

        header.addView(back)
        header.addView(title)
        root.addView(header)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        fun addTab(name: String, action: () -> Unit) {
            val button = Button(this).apply {
                text = name
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(panel)
                setOnClickListener { action() }
            }
            tabs.addView(button, LinearLayout.LayoutParams(0, dp(46), 1f))
        }

        addTab("Design") { showDesign() }
        addTab("Logic") { showLogic() }
        addTab("Config") { showConfig() }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this)
        scroll.addView(content)

        root.addView(tabs)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(root)
    }

    private fun showDesign() {
        content.removeAllViews()

        addLabel("COMPONENTES")

        val paletteScroll = HorizontalScrollView(this)
        val palette = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        componentTypes.forEach { type ->
            val button = Button(this).apply {
                text = type
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(panel)
                setOnClickListener { addComponent(type) }
            }

            palette.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(48)
            ).apply {
                setMargins(dp(3), dp(3), dp(3), dp(3))
            })
        }

        paletteScroll.addView(palette)
        content.addView(paletteScroll)

        addLabel("PRÉVIA DA TELA")
        addLabel("Toque em um componente para editar suas propriedades.")

        preview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minimumHeight = dp(400)
        }

        content.addView(preview, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(450)
        ))

        renderPreview()
    }

    private fun addComponent(type: String) {
        val item = JSONObject()
        item.put("type", type)
        item.put("text", when (type) {
            "Texto" -> "Novo texto"
            "Botão" -> "Novo botão"
            "Campo de texto" -> ""
            "Checkbox", "Radio Button", "Switch" -> "Opção"
            else -> ""
        })

        item.put("textSize", "18")
        item.put("textColor", "#000000")
        item.put("backgroundColor", when (type) {
            "Container" -> "#EEEEEE"
            "Divisor" -> "#999999"
            else -> "#FFFFFF"
        })
        item.put("hint", "Digite aqui")
        item.put("imagePath", "")
        item.put("scaleType", "FIT_CENTER")
        item.put("inputType", "Texto")
        item.put("checked", "false")
        item.put("width", "match")
        item.put("height", "wrap")
        item.put("margin", "8")
        item.put("alignment", "left")
        item.put("visible", "true")

        components.put(item)
        saveLayout()
        renderPreview()
    }

    private fun renderPreview() {
        if (!::preview.isInitialized) return

        preview.removeAllViews()

        for (index in 0 until components.length()) {
            val item = components.getJSONObject(index)
            val type = item.optString("type")
            val text = item.optString("text")

            val view: View = when (type) {
                "Texto" -> TextView(this).apply {
                    this.text = text
                    textSize = item.optString("textSize", "18").toFloatOrNull() ?: 18f
                    setTextColor(parseColor(item.optString("textColor"), Color.BLACK))
                }

                "Botão" -> Button(this).apply {
                    this.text = text
                    setTextColor(parseColor(item.optString("textColor"), Color.BLACK))
                    backgroundTintList = ColorStateList.valueOf(
                        parseColor(item.optString("backgroundColor"), Color.LTGRAY)
                    )
                }

                "Imagem" -> TextView(this).apply {
                    this.text = if (item.optString("imagePath").isBlank())
                        "[ Imagem ]"
                    else "[ ${item.optString("imagePath")} ]"
                    gravity = Gravity.CENTER
                    setTextColor(Color.DKGRAY)
                    setBackgroundColor(Color.LTGRAY)
                }

                "Campo de texto" -> EditText(this).apply {
                    hint = item.optString("hint", "Digite aqui")
                    setText(text)
                    setTextColor(Color.BLACK)
                    setHintTextColor(Color.GRAY)
                }

                "Checkbox" -> CheckBox(this).apply {
                    this.text = text
                    isChecked = item.optString("checked") == "true"
                    setTextColor(Color.BLACK)
                }

                "Radio Button" -> RadioButton(this).apply {
                    this.text = text
                    isChecked = item.optString("checked") == "true"
                    setTextColor(Color.BLACK)
                }

                "Switch" -> Switch(this).apply {
                    this.text = text
                    isChecked = item.optString("checked") == "true"
                    setTextColor(Color.BLACK)
                }

                "Container" -> LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(18), dp(12), dp(18))
                    setBackgroundColor(
                        parseColor(item.optString("backgroundColor"), Color.LTGRAY)
                    )
                    addView(TextView(this@ProjectEditorActivity).apply {
                        this.text = "Container"
                        setTextColor(Color.DKGRAY)
                    })
                }

                "Divisor" -> View(this).apply {
                    setBackgroundColor(
                        parseColor(item.optString("backgroundColor"), Color.GRAY)
                    )
                }

                else -> View(this)
            }

            view.visibility = if (item.optString("visible") == "false")
                View.GONE else View.VISIBLE

            if (view is TextView) {
                view.gravity = when (item.optString("alignment", "left")) {
                    "center" -> Gravity.CENTER
                    "right" -> Gravity.RIGHT or Gravity.CENTER_VERTICAL
                    else -> Gravity.LEFT or Gravity.CENTER_VERTICAL
                }
            }

            val width = dimension(item.optString("width"), true)
            val height = dimension(item.optString("height"), false)
            val margin = item.optString("margin", "8").toIntOrNull() ?: 8

            val wrapper = FrameLayout(this)
            val selected = selectedIndex == index

            wrapper.background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(
                    dp(if (selected) 2 else 1),
                    if (selected) green else Color.LTGRAY
                )
                cornerRadius = dp(5).toFloat()
            }

            wrapper.addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            val params = LinearLayout.LayoutParams(width, height)
            params.setMargins(dp(margin), dp(margin), dp(margin), dp(margin))
            preview.addView(wrapper, params)

            wrapper.setOnClickListener {
                selectedIndex = index
                renderPreview()
                showComponentActions(index)
            }
        }
    }

    private fun editComponent(index: Int) {
        val item = components.getJSONObject(index)
        val type = item.optString("type")
        val fields = linkedMapOf<String, EditText>()

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }

        val scroll = ScrollView(this)
        scroll.addView(form)

        fun addField(label: String, key: String) {
            form.addView(TextView(this).apply {
                text = label
                setTextColor(Color.BLACK)
                textSize = 14f
            })

            val input = EditText(this).apply {
                setText(item.optString(key, ""))
                textSize = 15f
                setSingleLine(true)
            }

            form.addView(input)
            fields[key] = input
        }

        when (type) {
            "Texto" -> {
                addField("Conteúdo", "text")
                addField("Tamanho do texto", "textSize")
                addField("Cor do texto (#RRGGBB)", "textColor")
            }

            "Botão" -> {
                addField("Texto do botão", "text")
                addField("Cor do texto (#RRGGBB)", "textColor")
                addField("Cor de fundo (#RRGGBB)", "backgroundColor")
            }

            "Imagem" -> {
                addField("Caminho da imagem", "imagePath")
                addField("Escala", "scaleType")
            }

            "Campo de texto" -> {
                addField("Dica", "hint")
                addField("Texto inicial", "text")
                addField("Tipo de entrada", "inputType")
            }

            "Checkbox", "Radio Button", "Switch" -> {
                addField("Texto", "text")
                addField("Marcado? (true/false)", "checked")
            }

            "Container", "Divisor" -> {
                addField("Cor de fundo (#RRGGBB)", "backgroundColor")
            }
        }

        val colorKeys = fields.keys.filter {
            it == "textColor" || it == "backgroundColor"
        }

        for (key in colorKeys) {
            val colors = arrayOf(
                "Preto" to "#000000",
                "Branco" to "#FFFFFF",
                "Verde" to "#65E6A5",
                "Azul" to "#2196F3",
                "Vermelho" to "#F44336",
                "Amarelo" to "#FFEB3B",
                "Roxo" to "#9C27B0",
                "Cinza" to "#9E9E9E"
            )

            val colorButton = Button(this).apply {
                text = "Escolher cor"
                setOnClickListener {
                    AlertDialog.Builder(this@ProjectEditorActivity)
                        .setTitle("Escolha uma cor")
                        .setItems(colors.map { it.first }.toTypedArray()) { _, choice ->
                            fields[key]?.setText(colors[choice].second)
                        }
                        .show()
                }
            }
            form.addView(colorButton)
        }

        form.addView(TextView(this).apply {
            text = "PROPRIEDADES GERAIS"
            setTextColor(Color.BLACK)
            textSize = 16f
            setPadding(0, dp(16), 0, dp(8))
        })

        addField("Largura (match ou valor em dp)", "width")
        addField("Altura (wrap ou valor em dp)", "height")
        addField("Margem (dp)", "margin")
        addField("Alinhamento (left, center, right)", "alignment")
        addField("Visível? (true/false)", "visible")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Propriedades: $type")
            .setView(scroll)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                fields.forEach { (key, input) ->
                    item.put(key, input.text.toString())
                }

                saveLayout()
                renderPreview()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showLogic() {
        content.removeAllViews()
        addLabel("EDITOR DE LÓGICA")
        addLabel("Blocos")
        addLabel("Java2Block")
        addLabel("Custom Blocks")
        addLabel("Em desenvolvimento")
    }

    private fun showConfig() {
        content.removeAllViews()
        addLabel("CONFIGURAÇÕES")
        addLabel("Nome: $projectName")
        addLabel("Pacote: $packageName")
        addLabel("Formato: .dvs")
        addLabel("Layout salvo em: screens/main.json")
    }

    private fun loadLayout() {
        val file = File(projectFolder, "screens/main.json")

        if (!file.exists()) return

        try {
            val loaded = JSONArray(file.readText())
            for (i in 0 until loaded.length()) {
                components.put(loaded.getJSONObject(i))
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível carregar o layout.", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveLayout() {
        try {
            val file = File(projectFolder, "screens/main.json")
            file.parentFile?.mkdirs()
            file.writeText(components.toString(2))
        } catch (_: Exception) {
            Toast.makeText(this, "Erro ao salvar o layout.", Toast.LENGTH_LONG).show()
        }
    }

    private fun dimension(value: String, isWidth: Boolean): Int {
        return when (value) {
            "match" -> if (isWidth)
                LinearLayout.LayoutParams.MATCH_PARENT
            else LinearLayout.LayoutParams.WRAP_CONTENT

            "wrap" -> LinearLayout.LayoutParams.WRAP_CONTENT
            else -> dp(value.toIntOrNull() ?: 100)
        }
    }

    private fun parseColor(value: String, fallback: Int): Int {
        return try {
            Color.parseColor(value)
        } catch (_: Exception) {
            fallback
        }
    }

    private fun addLabel(value: String) {
        content.addView(TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(4), dp(10), dp(4), dp(10))
        })
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun duplicateComponent(index: Int) {
        if (index !in 0 until components.length()) return

        val original = components.getJSONObject(index)
        val copy = JSONObject(original.toString())

        val updated = JSONArray()
        for (i in 0 until components.length()) {
            updated.put(components.getJSONObject(i))
            if (i == index) updated.put(copy)
        }

        replaceComponents(updated)
    }

    private fun deleteComponent(index: Int) {
        if (index !in 0 until components.length()) return

        val updated = JSONArray()
        for (i in 0 until components.length()) {
            if (i != index) updated.put(components.getJSONObject(i))
        }

        replaceComponents(updated)
    }

    private fun moveComponent(index: Int, direction: Int) {
        val target = index + direction
        if (index !in 0 until components.length()) return
        if (target !in 0 until components.length()) return

        val updated = JSONArray()
        for (i in 0 until components.length()) {
            when (i) {
                index -> updated.put(components.getJSONObject(target))
                target -> updated.put(components.getJSONObject(index))
                else -> updated.put(components.getJSONObject(i))
            }
        }

        replaceComponents(updated)
    }

    private fun replaceComponents(updated: JSONArray) {
        while (components.length() > 0) {
            components.remove(components.length() - 1)
        }

        for (i in 0 until updated.length()) {
            components.put(updated.getJSONObject(i))
        }

        saveLayout()
        renderPreview()
    }

    private fun showComponentActions(index: Int) {
        val item = components.optJSONObject(index) ?: return

        val actions = arrayOf(
            "Editar propriedades",
            "Duplicar",
            "Mover para cima",
            "Mover para baixo",
            "Excluir"
        )

        AlertDialog.Builder(this)
            .setTitle(item.optString("type"))
            .setItems(actions) { _, choice ->
                when (choice) {
                    0 -> editComponent(index)
                    1 -> duplicateComponent(index)
                    2 -> moveComponent(index, -1)
                    3 -> moveComponent(index, 1)
                    4 -> AlertDialog.Builder(this)
                        .setTitle("Excluir componente?")
                        .setMessage("Essa ação não pode ser desfeita.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Excluir") { _, _ ->
                            deleteComponent(index)
                        }
                        .show()
                }
            }
            .show()
    }

}
