package top.ntutn.ktwebio

import kotlinx.coroutines.delay

private suspend fun KTWebIO.storyDemo() {
    while (true) {
        putText("从前有座山")
        delay(3000)
        putText("山上有座庙")
        delay(3000)
        putText("庙里有两个和尚")
        delay(3000)
        putText("老和尚在给小和尚讲故事")
        delay(3000)
        putText("他讲的是")
        delay(3000)
        clearContent()
    }
}

private suspend fun KTWebIO.inputDemo() {
    putText("这是一个用户输入示例")
    input("test")
    input("test1")
    input("test2")
    delay(100_000)
}

fun main() = webIOBlock {
    inputDemo()
}