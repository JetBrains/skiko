package org.jetbrains.skiko.ios

import platform.UIKit.*

interface UIKitKeyboardOptions {

    fun keyboardType(): UIKeyboardType =
        UIKeyboardTypeDefault

    fun keyboardAppearance(): UIKeyboardAppearance =
        UIKeyboardAppearanceDefault

    fun returnKeyType(): UIReturnKeyType =
        UIReturnKeyType.UIReturnKeyDefault

    fun textContentType(): UITextContentType? =
        null

    fun isSecureTextEntry(): Boolean =
        false

    fun enablesReturnKeyAutomatically(): Boolean =
        false

    fun autocapitalizationType(): UITextAutocapitalizationType =
        UITextAutocapitalizationType.UITextAutocapitalizationTypeSentences

    fun autocorrectionType(): UITextAutocorrectionType =
        UITextAutocorrectionType.UITextAutocorrectionTypeYes

}
