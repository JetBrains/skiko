package org.jetbrains.skiko.benchmarks

internal actual fun postBenchmarkResult(url: String, name: String, report: String, token: String) {
    installBenchmarkFetchWithRetry()
    postBenchmarkResultJs(url, name, report, token)
}

internal actual fun stopBenchmarkServer(url: String, token: String) {
    installBenchmarkFetchWithRetry()
    stopBenchmarkServerJs(url, token)
}

internal actual fun decodeUrlComponent(value: String): String =
    decodeUrlComponentJs(value)

@Suppress("UNUSED_PARAMETER")
private fun postBenchmarkResultJs(url: String, name: String, report: String, token: String): Unit = js(
    """
    const post = () => fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name, stats: report, token: token })
        }).then(response => {
            if (response.status === 409) return;
            if (!response.ok) throw new Error('HTTP ' + response.status);
        });
    window.skikoBenchmarkPostChain = (window.skikoBenchmarkPostChain || Promise.resolve())
        .then(() => window.skikoBenchmarkFetchWithRetry(post, 'post ' + name));
    """
)

@Suppress("UNUSED_PARAMETER")
private fun stopBenchmarkServerJs(url: String, token: String): Unit = js(
    """
    const post = () => fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: '', stats: '', token: token })
        }).then(response => {
            if (response.status === 409) return;
            if (!response.ok) throw new Error('HTTP ' + response.status);
        });
    window.skikoBenchmarkPostChain = (window.skikoBenchmarkPostChain || Promise.resolve())
        .then(() => window.skikoBenchmarkFetchWithRetry(post, 'stop server'))
        .then(() => {
            const status = document.getElementById('status');
            if (status) status.textContent = 'Done';
        })
        .catch(error => {
            console.error(error);
            const status = document.getElementById('status');
            if (status) status.textContent = 'Failed to post benchmark results: ' + error.message;
        });
    """
)

@Suppress("UNUSED_PARAMETER", "UnsafeCastFromDynamic")
private fun decodeUrlComponentJs(value: String): String = js("decodeURIComponent(value)")

private fun installBenchmarkFetchWithRetry(): Unit = js(
    """
    if (!window.skikoBenchmarkFetchWithRetry) {
        window.skikoBenchmarkFetchWithRetry = (operation, label, attempt = 1) => {
            return operation().catch(error => {
                if (attempt >= 20) {
                    throw new Error(label + ' failed after ' + attempt + ' attempts: ' + error.message);
                }
                return new Promise(resolve => setTimeout(resolve, 250))
                    .then(() => window.skikoBenchmarkFetchWithRetry(operation, label, attempt + 1));
            });
        };
    }
    """
)
