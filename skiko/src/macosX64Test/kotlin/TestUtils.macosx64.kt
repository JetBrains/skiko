import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual fun runTest(block: suspend () -> Unit) {
    GlobalScope.launch {
        block()
    }
}
