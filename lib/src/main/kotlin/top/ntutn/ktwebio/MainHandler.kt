package top.ntutn.ktwebio

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormEncodedDataDefinition
import io.undertow.server.handlers.form.FormParserFactory
import io.undertow.server.handlers.form.MultiPartParserDefinition
import io.undertow.util.AttachmentKey
import io.undertow.util.Headers
import io.undertow.util.PathMatcher
import io.undertow.util.StatusCodes
import java.util.concurrent.ConcurrentLinkedQueue

class MainHandler: HttpHandler {
    val openPageWaiter = ResponseWaiter()
    val contentViewedWaiter = ResponseWaiter()

    private val contentBuffer = mutableListOf<IWebIOContent>()
    private var serverContentVersion = 0L
    private var clientContentVersion = 0L

    private val pathMatcher = PathMatcher<(HttpServerExchange) -> Unit>()
    private val parserFactory = FormParserFactory.builder().addParsers(FormEncodedDataDefinition(), MultiPartParserDefinition()).build()
    private val formDataKey = AttachmentKey.create(FormData::class.java);

    private var formData: FormData? = null

    init {
        pathMatcher.addExactPath("/", ::mainPage)
        pathMatcher.addExactPath("/version", ::updateContentVersion)
        pathMatcher.addExactPath("/submit", ::updateContentVersion)
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
        val (serverVersion, contentString) = synchronized(contentBuffer) {
            serverContentVersion to contentBuffer.joinToString("\n", transform = IWebIOContent::getHtml)
        }

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
                    <span id="online_badge" class="badge bg-success">Online</span>
                    <span id="offline_badge" class="badge bg-danger" style="display:none">Offline</span>
                  </div>
                  <div class="card-body">
                    $contentString
                  </div>
                  <div class="card-footer text-muted">
                    Powered by <a href="https://github.com/zerofancy/ktwebio" class="card-link">KTWebIO</a>
                  </div>
                </div>
            </div>
            <script>
                const intervalId = setInterval(function() {
                    fetch("version?version=$serverVersion")
                        .then((response) => response.json())
                        .then(function(value) {
                            if (value != $serverVersion) {
                                location.reload()
                            }
                        })
                        .catch(function(e) {
                            clearInterval(intervalId)
                            document.getElementById("online_badge").style.display = "none"
                            document.getElementById("offline_badge").style.display = "inline"                            
                        })
                }, 1000)
            </script>
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
        exchange.responseSender.send(serverContentVersion.toString())
    }

    private fun submitUserInput(exchange: HttpServerExchange) {
        formData = exchange.getAttachment(formDataKey)

        exchange.setStatusCode(StatusCodes.FOUND)
        exchange.responseHeaders.put(Headers.LOCATION, "/")
        exchange.endExchange()
    }
}