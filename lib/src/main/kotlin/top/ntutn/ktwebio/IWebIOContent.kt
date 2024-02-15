package top.ntutn.ktwebio

import kotlinx.serialization.Serializable

interface IWebIOContent {
    fun getHtml(): String
}

@Serializable
data class WebIOContent(val type: Int, val content: String) {
    companion object {
        enum class Type {
            TextOutput
        }
    }
}
