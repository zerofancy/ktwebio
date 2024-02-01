package top.ntutn.ktwebio

import java.util.UUID

class TextInputContent(key: String? = null): InputContent() {
    private val fieldName: String = key ?: UUID.randomUUID().toString()

    override fun getHtml(): String = """
        <input name="$fieldName"/>
    """.trimIndent()
}