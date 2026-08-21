package org.jetbrains.skiko.benchmarks

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun postBenchmarkResult(url: String, name: String, report: String, token: String) {
    installBenchmarkFetchWithRetry()
    postBenchmarkResultWasm(url, name, report, token)
}

internal actual fun stopBenchmarkServer(url: String, token: String) {
    installBenchmarkFetchWithRetry()
    stopBenchmarkServerWasm(url, token)
}

internal actual fun decodeUrlComponent(value: String): String =
    decodeUrlComponentWasm(value)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(url, name, report, token) => {
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
    }"""
)
private external fun postBenchmarkResultWasm(url: String, name: String, report: String, token: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(url, token) => {
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
    }"""
)
private external fun stopBenchmarkServerWasm(url: String, token: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(value) => decodeURIComponent(value)")
private external fun decodeUrlComponentWasm(value: String): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        if (window.skikoBenchmarkFetchWithRetry) return;
        window.skikoBenchmarkFetchWithRetry = (operation, label, attempt = 1) => {
            return operation().catch(error => {
                if (attempt >= 20) {
                    throw new Error(label + ' failed after ' + attempt + ' attempts: ' + error.message);
                }
                return new Promise(resolve => setTimeout(resolve, 250))
                    .then(() => window.skikoBenchmarkFetchWithRetry(operation, label, attempt + 1));
            });
        };
    }"""
)
private external fun installBenchmarkFetchWithRetry()
