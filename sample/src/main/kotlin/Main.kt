package top.ntutn.ktwebio

import io.undertow.server.handlers.form.FormData
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
    val name = inputSuspend("姓名")
    putText("你好, $name, 接下来我们试试一次性输入多个值。")
    input("sex", "性别", value = "男")
    input("age", "年龄")
    val data = formInput()
    data?.let { data ->
        data.forEach {
            val value = data[it].joinToString(transform = FormData.FormValue::getValue)
            putText("$it:$value")
        }
    }
}

fun main() = webIOBlock {
    storyDemo()
}