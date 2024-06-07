package experiments.aichat.console

import kotlinx.coroutines.*

class Loader {

    private var stop = false

    fun <T> withLoader(message: String = "Processing", f: suspend () -> T): T = runBlocking {
        val animationJob = launch {
            showAnimation(message)
        }
        val result = withContext(Dispatchers.Default) {
            f()
        }
        delay(150)
        animationJob.hideAnimation()
        result
    }

    private suspend fun showAnimation(message: String) {
        stop = false
        val animationChars = listOf("|", "/", "-", "\\")
        var i = 0
        while (!stop) {
            print("\r${animationChars[i++ % animationChars.size]} $message...")
            delay(100)
        }
        print("\r")
        println()
    }

    private suspend fun Job.hideAnimation() {
        stop = true
        join()
    }
}