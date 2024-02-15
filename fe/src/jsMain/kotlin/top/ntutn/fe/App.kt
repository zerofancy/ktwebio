package top.ntutn.fe

import io.kvision.Application
import io.kvision.CoreModule
import io.kvision.BootstrapModule
import io.kvision.BootstrapCssModule
import io.kvision.DatetimeModule
import io.kvision.RichTextModule
import io.kvision.BootstrapUploadModule
import io.kvision.FontAwesomeModule
import io.kvision.html.*
import io.kvision.i18n.tr
import io.kvision.module
import io.kvision.panel.responsiveGridPanel
import io.kvision.panel.root
import io.kvision.rest.RestClient
import io.kvision.rest.call
import io.kvision.startApplication
import io.kvision.state.ObservableList
import io.kvision.state.bind
import io.kvision.state.observableListOf
import io.kvision.toolbar.buttonGroup
import io.kvision.toolbar.toolbar
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.js.Promise

@Serializable
data class Query(val q: String?)

@Serializable
data class JsonContentWrapper(val version: Long, val content: List<WebIOContent>)

@Serializable
data class WebIOContent(val type: Int, val content: String)

class App : Application() {
    override fun start() {
        val texts = observableListOf<String>()
        root("kvapp") {
            header {
                nav(className = "navbar navbar-expand-lg navbar-light bg-light") {
                    div(className = "container-fluid") {
                        span("KTWebIO", className = "navbar-brand")
                    }
                }
            }
            main(className = "container").bind(texts) {
                it.forEach {
                    div(className = "card mt-4") {
                        div(it, className = "card-body")
                    }
                }
                div(className = "mt-4")
            }
            footer(className = "container alert alert-light") {
                p("Powered by <a href=\"https://github.com/zerofancy/ktwebio\">KTWebIO</a>.", rich = true)
            }
        }
        GlobalScope.launch {
            val restClient = RestClient()
            while (true) {
                val result = restClient.call<JsonContentWrapper, Query>("/api/json", Query("kvision"))
                    .await()
                console.log(result.toString())
                texts.clear()
                texts.addAll(result.content.map { it.content })
                delay(3000)
            }

        }
    }
}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        DatetimeModule,
        RichTextModule,
        BootstrapUploadModule,
        FontAwesomeModule,
        CoreModule
    )
}
