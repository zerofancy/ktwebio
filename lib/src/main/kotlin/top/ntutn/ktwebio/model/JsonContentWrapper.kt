package top.ntutn.ktwebio.model

import kotlinx.serialization.Serializable
import top.ntutn.ktwebio.WebIOContent

@Serializable
data class JsonContentWrapper(val version: Long, val content: List<WebIOContent>)
