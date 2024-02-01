package top.ntutn.ktwebio

import org.apache.commons.text.StringEscapeUtils

class StringWebContent(private val content: String): OutputContent() {
    override fun getHtml(): String {
        return StringEscapeUtils.escapeHtml4(content).let { "<p>$it</p>" }
    }
}