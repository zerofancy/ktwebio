package top.ntutn.ktwebio

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.PathMatcher
import io.undertow.util.StatusCodes

class MainHandler: HttpHandler {
    val openPageWaiter = ResponseWaiter()
    val contentViewedWaiter = ResponseWaiter()

    private val contentBuffer = mutableListOf<String>()
    private var serverContentVersion = 0L
    private var clientContentVersion = 0L

    private val pathMatcher = PathMatcher<(HttpServerExchange) -> Unit>()

    init {
        addContent("Hello World!")

        pathMatcher.addExactPath("/", ::mainPage)
        pathMatcher.addExactPath("/version", ::updateContentVersion)
    }

    fun addContent(content: String) {
        synchronized(contentBuffer) {
            contentBuffer.add(content)
            serverContentVersion++
        }
    }

    override fun handleRequest(exchange: HttpServerExchange) {
        val match = pathMatcher.match(exchange.relativePath)
        match.value?.invoke(exchange) ?: exchange.setStatusCode(StatusCodes.NOT_FOUND)
    }

    private fun mainPage(exchange: HttpServerExchange) {
        openPageWaiter.notifyEvent()

        val html = """
            <html>
            <head>
            <title>KtWebIO</title>
            </head>
            <body>
            <h1>KTWebIO</h1>
            ${contentBuffer.joinToString("<br />")}
            <script src="version?version=$serverContentVersion" />
            </body>
            </html>
        """.trimIndent()
        exchange.responseSender.send(html)
    }

    private fun updateContentVersion(exchange: HttpServerExchange) {
        val version = exchange.queryParameters["version"]?.firstOrNull()?.toLongOrNull()
        if (version == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST)
            return
        }
        clientContentVersion = version
        if (clientContentVersion == serverContentVersion) {
            contentViewedWaiter.notifyEvent()
        }
        exchange.responseSender.send("")
    }
}