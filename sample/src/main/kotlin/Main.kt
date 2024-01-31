package top.ntutn.ktwebio

import kotlinx.coroutines.delay

fun main() = webIOBlock {
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