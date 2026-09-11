package org.jetbrains.skiko.ios

import platform.UIKit.*

/**
 * Represents a part of UITextInputTraits protocol. Needs to control onscreen keyboard features.
 * https://developer.apple.com/documentation/uikit/uitextinputtraits?language=objc
 */
interface SkikoUITextInputTraits {

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

    fun spellCheckingType(): UITextSpellCheckingType =
        UITextSpellCheckingType.UITextSpellCheckingTypeDefault

    fun smartQuotesType(): UITextSmartQuotesType =
        UITextSmartQuotesType.UITextSmartQuotesTypeDefault

    fun smartDashesType(): UITextSmartDashesType =
        UITextSmartDashesType.UITextSmartDashesTypeDefault

    fun smartInsertDeleteType(): UITextSmartInsertDeleteType =
        UITextSmartInsertDeleteType.UITextSmartInsertDeleteTypeDefault

}
