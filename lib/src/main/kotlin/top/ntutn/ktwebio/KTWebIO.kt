package top.ntutn.ktwebio

import io.undertow.Undertow
import io.undertow.UndertowOptions
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.URI
import java.util.UUID

/**
 * webjars路径示例 http://localhost:39861/webjars/bootstrap/5.3.2/js/bootstrap.min.js
 */
suspend fun webIOScope(block: suspend KTWebIO.() -> Unit) = withContext(Dispatchers.IO) {
    val s = ServerSocket(0)
    val port = s.localPort
    s.close()

    val webIO = KTWebIO()
    val handler = PathHandler().apply {
        addExactPath("favicon.ico", ResourceHandler(ClassPathResourceManager(javaClass.classLoader, "webio/static/favicon.ico")))
        addPrefixPath(
            "/webjars",
            ResourceHandler(ClassPathResourceManager(javaClass.classLoader, "META-INF/resources/webjars"))
        )
        addPrefixPath("/", webIO.httpHandler)
    }
    val server = Undertow.builder()
        .addHttpListener(port, "localhost")
        .setHandler(handler)
        .setServerOption(UndertowOptions.URL_CHARSET, "UTF-8")
        .build()
    server.start()
    val url = URI("http://localhost:${port}")
    println("Local url $url")
    try {
        DesktopUtil.openInBrowser(url)
    } catch (_: NotImplementedError) {
        println("Open browser failed.")
    }

    // wait url open
    webIO.httpHandler.openPageWaiter.waitEvent()
    println("You opened page.")
    block(webIO)
    webIO.httpHandler.contentViewedWaiter.waitEvent()
    delay(3_000) // 延迟一点，避免最后一页的静态资源没有加载好
    server.stop()
}

fun webIOBlock(block: suspend KTWebIO.() -> Unit) = runBlocking {
    webIOScope(block)
}

class KTWebIO {
    internal val httpHandler = MainHandler()

    fun putText(content: String) {
        httpHandler.addContent(StringWebContent(content))
    }

    fun clearContent() {
        httpHandler.clearContent()
    }

    fun input(name: String) { // nowait vs wait
        httpHandler.addContent(TextInputContent(name))
    }

    suspend fun input(): String? {
        val key = UUID.randomUUID().toString()
        httpHandler.addContent(TextInputContent(key))
        httpHandler.submitWaiter.waitEvent()
        val value = httpHandler.formData?.get(key)?.first?.value
        return value
    }

    suspend fun formInput(): FormData? {
        httpHandler.submitWaiter.waitEvent()
        return httpHandler.formData
    }
}