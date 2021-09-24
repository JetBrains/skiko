import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

annotation class DoNothing
actual typealias IgnoreTestOnJvm = DoNothing

actual fun runTest(block: suspend () -> Unit) {
    GlobalScope.launch {
        block()
    }
}
