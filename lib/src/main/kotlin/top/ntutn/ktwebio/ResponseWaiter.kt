package top.ntutn.ktwebio

import kotlinx.coroutines.channels.Channel

class ResponseWaiter {
    private val channel = Channel<Unit>(0)

    suspend fun waitEvent() {
        channel.receive()
    }

    fun notifyEvent() {
        channel.trySend(Unit)
    }
}