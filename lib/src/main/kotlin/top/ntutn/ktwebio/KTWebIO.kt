package top.ntutn.ktwebio

import io.undertow.Undertow
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.Headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.URI

/**
 * webjars路径示例 http://localhost:39861/webjars/bootstrap/5.3.2/js/bootstrap.min.js
 */
suspend fun webIOScope(block: suspend KTWebIO.() -> Unit) = withContext(Dispatchers.IO) {
    val s = ServerSocket(0)
    val port = s.localPort
    s.close()

    val waiter = ResponseWaiter()
    val handler = PathHandler().apply {
        addExactPath("/") { exchange ->
            exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
            exchange.responseSender.send("Hello World")
            waiter.notifyEvent()
        }.addPrefixPath("/webjars", ResourceHandler(ClassPathResourceManager(javaClass.classLoader, "META-INF/resources/webjars")))
    }
    val server = Undertow.builder()
        .addHttpListener(port, "localhost")
        .setHandler(handler)
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
    waiter.waitEvent()
    println("You opened page.")
    block(KTWebIO)
    server.stop()
}

fun webIOBlock(block: suspend KTWebIO.() -> Unit) = runBlocking {
    webIOScope(block)
}

object KTWebIO {
    suspend fun input() {
        while (true) {
            delay(60_000)
        }
    }
}