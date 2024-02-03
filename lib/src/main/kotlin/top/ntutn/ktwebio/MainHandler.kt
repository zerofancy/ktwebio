package top.ntutn.ktwebio

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormDataParser
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
    val submitWaiter = ResponseWaiter()

    private val contentBuffer = mutableListOf<IWebIOContent>()
    private var serverContentVersion = 0L
    private var clientContentVersion = 0L

    private val pathMatcher = PathMatcher<(HttpServerExchange) -> Unit>()
    private val parserFactory = FormParserFactory.builder(false)
        .addParser(FormEncodedDataDefinition().also {
            it.setDefaultEncoding("UTF-8")
        })
        .addParser(MultiPartParserDefinition().also {
            it.setDefaultEncoding("UTF-8")
        })
        .build()
    private val formDataKey = FormDataParser.FORM_DATA

    var formData: FormData? = null
        private set

    init {
        pathMatcher.addExactPath("/", ::mainPage)
        pathMatcher.addExactPath("/version", ::updateContentVersion)
        pathMatcher.addExactPath("/submit", ::submitUserInput)
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
        val (serverVersion, contentString, buttonJs) = synchronized(contentBuffer) {
            val content = contentBuffer.joinToString("\n", transform = IWebIOContent::getHtml)
            // todo using form data
            val inputContents = contentBuffer.filterIsInstance<InputContent>()
            val (buttonHtml, buttonJs) = if (inputContents.isNotEmpty()) {
                """
                    <button id="form_submit" type="button" class="btn btn-primary">Submit</button>
                """.trimIndent() to """
                    const formData = new FormData()
                    ${inputContents.joinToString("\n", transform = InputContent::readValueJs)}
                """.trimIndent()
            } else {
                "" to ""
            }
            Triple(serverContentVersion, content + buttonHtml, buttonJs)
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
                const submitButton = document.getElementById("form_submit")
                if (submitButton !== null) {
                    submitButton.addEventListener("click", function() {
                        $buttonJs
                        // Display the key/value pairs
                        for (var pair of formData.entries())
                        {
                         console.log(pair[0]+ ', '+ pair[1]); 
                        }
                        fetch("submit", {
                            method: "POST",
                            body: formData
                        })
                        .then(function(value) {
                            location.reload()
                        })
                    })
                }
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
        exchange.startBlocking()
        val parser = parserFactory.createParser(exchange)

        parser.parse {
            exchange.responseSender.send("")
            formData = it.getAttachment(formDataKey)
            submitWaiter.notifyEvent()
            clearContent()
        }
    }
}