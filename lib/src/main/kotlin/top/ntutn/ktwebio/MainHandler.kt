package top.ntutn.ktwebio

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.PathMatcher
import io.undertow.util.StatusCodes

class MainHandler: HttpHandler {
    val openPageWaiter = ResponseWaiter()
    val contentViewedWaiter = ResponseWaiter()

    private val contentBuffer = mutableListOf<IWebIOContent>()
    private var serverContentVersion = 0L
    private var clientContentVersion = 0L

    private val pathMatcher = PathMatcher<(HttpServerExchange) -> Unit>()

    init {
        pathMatcher.addExactPath("/", ::mainPage)
        pathMatcher.addExactPath("/version", ::updateContentVersion)
    }

    fun addContent(content: IWebIOContent) {
        synchronized(contentBuffer) {
            contentBuffer.add(content)
            serverContentVersion++
        }
    }

    fun clearContent() = synchronized(contentBuffer) {
        contentBuffer.clear()
        serverContentVersion++
    }

    override fun handleRequest(exchange: HttpServerExchange) {
        val match = pathMatcher.match(exchange.relativePath)
        match.value?.invoke(exchange) ?: exchange.setStatusCode(StatusCodes.NOT_FOUND)
    }

    private fun mainPage(exchange: HttpServerExchange) {
        openPageWaiter.notifyEvent()

        val html = """
            <!doctype html>
            <html lang="zh-Hans">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link href="webjars/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
            <title>KtWebIO</title>
            </head>
            <body>
            <div class="container">
                <div class="card">
                  <div class="card-header">
                    KTWebIO
                  </div>
                  <div class="card-body">
                    ${contentBuffer.joinToString("\n", transform = IWebIOContent::getHtml)}
                  </div>
                  <div class="card-footer text-muted">
                    Powered by <a href="https://github.com/zerofancy/ktwebio" class="card-link">KTWebIO</a>
                  </div>
                </div>
            </div>
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