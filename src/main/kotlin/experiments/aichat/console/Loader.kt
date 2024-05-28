package experiments.aichat.console

import kotlinx.coroutines.*

class Loader {

    private var stop = false

    fun withLoader(f: () -> Any?): Any? = runBlocking {
        val animationJob = launch {
            showAnimation()
        }
        val result = withContext(Dispatchers.Default) {
            f()
        }
        delay(150)
        animationJob.hideAnimation()
        result
    }

    private suspend fun showAnimation() {
        stop = false
        val animationChars = listOf("|", "/", "-", "\\")
        var i = 0
        while (!stop) {
            print("\r${animationChars[i++ % animationChars.size]} Processing...")
            delay(100)
        }
        println()
    }

    private suspend fun Job.hideAnimation() {
        stop = true
        join()
    }
}