package top.ntutn.ktwebio

import java.util.UUID

class TextInputContent(key: String? = null): InputContent() {
    private val fieldName: String = key ?: UUID.randomUUID().toString()

    override fun getHtml(): String = """
        <input id="$fieldName"/>
    """.trimIndent()

    override fun readValueJs(): String = """
        formData.append("$fieldName", document.getElementById("$fieldName").value)
    """.trimIndent()
}