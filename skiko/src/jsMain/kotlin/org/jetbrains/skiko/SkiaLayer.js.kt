package org.jetbrains.skiko

import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.UIEvent

actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = KeyboardEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent

//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = UIEvent
