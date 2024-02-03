package top.ntutn.ktwebio

import java.util.UUID

class TextInputContent(key: String): InputContent() {
    private val fieldName: String = key

    override fun getHtml(): String = """
        <input id="$fieldName"/>
    """.trimIndent()

    override fun readValueJs(): String = """
        formData.append("$fieldName", document.getElementById("$fieldName").value)
    """.trimIndent()
}