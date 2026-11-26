@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import kotlin.js.JsAny
import kotlin.js.js

internal class BrowserFrameClock : WebFrameClock {
    private var channel: JsAny? = null
    private var visibilityListener: JsAny? = null
    private var active = false
    private var nextId = 0
    private val tasks = mutableMapOf<Int, () -> Unit>()

    override fun now() = browserNow()
    override fun requestAnimationFrame(callback: (Double) -> Unit) = browserRaf(callback)
    override fun cancelAnimationFrame(id: Int) = browserCancelRaf(id)
    override fun setTimeout(callback: () -> Unit, delay: Double) = browserTimeout(callback, delay)
    override fun cancelTimeout(id: Int) = browserCancelTimeout(id)

    override fun postTask(callback: () -> Unit): Int {
        val port = channel ?: createChannel { id -> tasks.remove(id)?.invoke() }.also { channel = it }
        val id = ++nextId
        tasks[id] = callback
        postChannel(port, id)
        return id
    }
    override fun cancelTask(id: Int) {
        tasks.remove(id)
    }

    override fun isVisible() = browserVisible()

    override fun setVisibilityListener(callback: (() -> Unit)?) {
        visibilityListener?.let(::removeVisibility)
        visibilityListener = callback?.let(::addVisibility)
    }

    override fun setHighRefreshRateActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        updateDisplayBoost(if (active) 1 else -1, displayBoost)
    }

    override fun close() {
        setHighRefreshRateActive(false)
        setVisibilityListener(null)
        channel?.let(::closeChannel)
        channel = null
        tasks.clear()
    }
}

private fun browserNow(): Double = js("performance.now()")
private fun browserRaf(callback: (Double) -> Unit): Int = js("requestAnimationFrame(callback)")
private fun browserCancelRaf(id: Int): Unit = js("cancelAnimationFrame(id)")
private fun browserTimeout(callback: () -> Unit, delay: Double): Int = js("setTimeout(callback, delay)")
private fun browserCancelTimeout(id: Int): Unit = js("clearTimeout(id)")
private fun browserVisible(): Boolean = js("!document.hidden")

// Keep the exact JS listener for removal on both Kotlin/JS and Kotlin/Wasm.
//language=JavaScript
private fun addVisibility(callback: () -> Unit): JsAny = js("""(function() {
    document.addEventListener('visibilitychange', callback);
    return callback;
})()""")
private fun removeVisibility(listener: JsAny): Unit = js("document.removeEventListener('visibilitychange', listener)")

//language=JavaScript
private fun createChannel(callback: (Int) -> Unit): JsAny = js("""(function() {
    var channel = new MessageChannel();
    channel.port1.onmessage = function(event) { callback(event.data); };
    return channel;
})()""")
private fun postChannel(channel: JsAny, id: Int): Unit = js("channel.port2.postMessage(id)")
private fun closeChannel(channel: JsAny): Unit = js("{ channel.port1.close(); channel.port2.close(); }")

// ProMotion needs scrolling plus a compositor animation to reach 120 Hz. Share them between active layers.
private val displayBoost: JsAny = js("({ users: 0, element: null, timer: null, animation: null })")
//language=JavaScript
private fun updateDisplayBoost(delta: Int, state: JsAny): Unit = js("""(function() {
    state.users += delta;
    if (state.users > 0 && !state.element && document.body) {
        var scroller = document.createElement('div');
        scroller.setAttribute('data-skiko-refresh-boost', '');
        scroller.setAttribute('aria-hidden', 'true');
        scroller.style.cssText = 'position:fixed;left:0;bottom:0;width:1px;height:1px;overflow:hidden;opacity:0.01;pointer-events:none;contain:strict';
        var animated = document.createElement('div');
        animated.style.cssText = 'position:sticky;top:0;width:1px;height:1px;background:black;will-change:transform';
        var spacer = document.createElement('div');
        spacer.style.height = '10000px';
        scroller.appendChild(animated);
        scroller.appendChild(spacer);
        document.body.appendChild(scroller);
        state.element = scroller;
        state.animation = animated.animate(
            [{ transform: 'translateX(0)' }, { transform: 'translateX(0.1px)' }],
            { duration: 1000, iterations: Infinity, direction: 'alternate' }
        );
        var down = true;
        var scroll = function() {
            clearTimeout(state.timer);
            if (state.element !== scroller) return;
            scroller.scrollTo({ top: down ? 9000 : 0, behavior: 'smooth' });
            down = !down;
            state.timer = setTimeout(scroll, 1500);
        };
        scroller.addEventListener('scrollend', scroll);
        state.timer = setTimeout(scroll, 0);
    } else if (state.users === 0 && state.element) {
        clearTimeout(state.timer);
        state.animation.cancel();
        state.element.remove();
        state.element = null;
        state.animation = null;
        state.timer = null;
    }
})()""")
