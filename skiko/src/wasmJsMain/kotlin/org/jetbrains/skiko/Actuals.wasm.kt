package org.jetbrains.skiko

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    setCursor(component as JsAny, cursor)
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return getCursor(component as JsAny)
}

@JsFun("(component, cursor) => { if (component && component.style) component.style.cursor = cursor; }")
private external fun setCursor(component: JsAny, cursor: Cursor)

@JsFun("(component) => component && component.style ? component.style.cursor : null")
private external fun getCursor(component: JsAny): Cursor?

internal actual fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform")
