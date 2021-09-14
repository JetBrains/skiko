package org.jetbrains.skia

/**
 * Information about individual frames in a multi-framed image.
 */
class AnimationFrameInfo(
    /**
     *
     * The frame that this frame needs to be blended with, or
     * -1 if this frame is independent (so it can be
     * drawn over an uninitialized buffer).
     *
     *
     * Note that this is the *earliest* frame that can be used
     * for blending. Any frame from [_requiredFrame, i) can be
     * used, unless its getDisposalMethod() is [AnimationDisposalMode.RESTORE_PREVIOUS].
     */
    var requiredFrame: Int,
    /**
     * Number of milliseconds to show this frame.
     */
    var duration: Int,
    /**
     *
     * Whether the end marker for this frame is contained in the stream.
     *
     *
     * Note: this does not guarantee that an attempt to decode will be complete.
     * There could be an error in the stream.
     */
    var isFullyReceived: Boolean,
    /**
     *
     * This is conservative; it will still return non-opaque if e.g. a
     * color index-based frame has a color with alpha but does not use it.
     */
    var alphaType: ColorAlphaType,
    /**
     *
     * Whether the updated rectangle contains alpha.
     *
     *
     * This is conservative; it will still be set to true if e.g. a color
     * index-based frame has a color with alpha but does not use it. In
     * addition, it may be set to true, even if the final frame, after
     * blending, is opaque.
     */
    var isHasAlphaWithinBounds: Boolean,
    /**
     *
     * How this frame should be modified before decoding the next one.
     */
    var disposalMethod: AnimationDisposalMode,
    /**
     *
     * How this frame should blend with the prior frame.
     */
    var blendMode: BlendMode,
    /**
     *
     * The rectangle updated by this frame.
     *
     *
     * It may be empty, if the frame does not change the image. It will
     * always be contained by [Codec.getSize].
     */
    internal var frameRect: IRect
) {
    internal constructor(
        requiredFrame: Int,
        duration: Int,
        fullyReceived: Boolean,
        alphaTypeOrdinal: Int,
        hasAlphaWithinBounds: Boolean,
        disposalMethodOrdinal: Int,
        blendModeOrdinal: Int,
        frameRect: IRect
    ) : this(
        requiredFrame,
        duration,
        fullyReceived,
        ColorAlphaType.values()[alphaTypeOrdinal],
        hasAlphaWithinBounds,
        AnimationDisposalMode.values()[disposalMethodOrdinal],
        BlendMode.values()[blendModeOrdinal],
        frameRect
    )
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is AnimationFrameInfo) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (requiredFrame != other.requiredFrame) return false
        if (duration != other.duration) return false
        if (isFullyReceived != other.isFullyReceived) return false
        if (isHasAlphaWithinBounds != other.isHasAlphaWithinBounds) return false
        val `this$_alphaType`: Any = alphaType
        val `other$_alphaType`: Any = other.alphaType
        if (`this$_alphaType` != `other$_alphaType`) return false
        val `this$_disposalMethod`: Any = disposalMethod
        val `other$_disposalMethod`: Any = other.disposalMethod
        if (`this$_disposalMethod` != `other$_disposalMethod`) return false
        val `this$_blendMode`: Any = blendMode
        val `other$_blendMode`: Any = other.blendMode
        if (`this$_blendMode` != `other$_blendMode`) return false
        val `this$_frameRect`: Any = frameRect
        val `other$_frameRect`: Any = other.frameRect
        return `this$_frameRect` == `other$_frameRect`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is AnimationFrameInfo
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + requiredFrame
        result = result * PRIME + duration
        result = result * PRIME + if (isFullyReceived) 79 else 97
        result = result * PRIME + if (isHasAlphaWithinBounds) 79 else 97
        val `$_alphaType`: Any = alphaType
        result = result * PRIME + (`$_alphaType`.hashCode())
        val `$_disposalMethod`: Any = disposalMethod
        result = result * PRIME + `$_disposalMethod`.hashCode()
        val `$_blendMode`: Any = blendMode
        result = result * PRIME + `$_blendMode`.hashCode()
        val `$_frameRect`: Any = frameRect
        result = result * PRIME + `$_frameRect`.hashCode()
        return result
    }

    override fun toString(): String {
        return "AnimationFrameInfo(_requiredFrame=$requiredFrame, _duration=$duration, _fullyReceived=$isFullyReceived, _alphaType=$alphaType, _hasAlphaWithinBounds=$isHasAlphaWithinBounds, _disposalMethod=$disposalMethod, _blendMode=$blendMode, _frameRect=$frameRect)"
    }

    /**
     *
     * The frame that this frame needs to be blended with, or
     * -1 if this frame is independent (so it can be
     * drawn over an uninitialized buffer).
     *
     *
     * Note that this is the *earliest* frame that can be used
     * for blending. Any frame from [_requiredFrame, i) can be
     * used, unless its getDisposalMethod() is [AnimationDisposalMode.RESTORE_PREVIOUS].
     * @return `this`.
     */
    fun withRequiredFrame(_requiredFrame: Int): AnimationFrameInfo {
        return if (requiredFrame == _requiredFrame) this else AnimationFrameInfo(
            _requiredFrame,
            duration,
            isFullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     * Number of milliseconds to show this frame.
     * @return `this`.
     */
    fun withDuration(_duration: Int): AnimationFrameInfo {
        return if (duration == _duration) this else AnimationFrameInfo(
            requiredFrame,
            _duration,
            isFullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     *
     * Whether the end marker for this frame is contained in the stream.
     *
     *
     * Note: this does not guarantee that an attempt to decode will be complete.
     * There could be an error in the stream.
     * @return `this`.
     */
    fun withFullyReceived(_fullyReceived: Boolean): AnimationFrameInfo {
        return if (isFullyReceived == _fullyReceived) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            _fullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     *
     * This is conservative; it will still return non-opaque if e.g. a
     * color index-based frame has a color with alpha but does not use it.
     * @return `this`.
     */
    fun withAlphaType(_alphaType: ColorAlphaType): AnimationFrameInfo {
        return if (alphaType == _alphaType) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            isFullyReceived,
            _alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     *
     * Whether the updated rectangle contains alpha.
     *
     *
     * This is conservative; it will still be set to true if e.g. a color
     * index-based frame has a color with alpha but does not use it. In
     * addition, it may be set to true, even if the final frame, after
     * blending, is opaque.
     * @return `this`.
     */
    fun withHasAlphaWithinBounds(_hasAlphaWithinBounds: Boolean): AnimationFrameInfo {
        return if (isHasAlphaWithinBounds == _hasAlphaWithinBounds) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            isFullyReceived,
            alphaType,
            _hasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     *
     * How this frame should be modified before decoding the next one.
     * @return `this`.
     */
    fun withDisposalMethod(_disposalMethod: AnimationDisposalMode): AnimationFrameInfo {
        return if (disposalMethod === _disposalMethod) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            isFullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            _disposalMethod,
            blendMode,
            frameRect
        )
    }

    /**
     *
     * How this frame should blend with the prior frame.
     * @return `this`.
     */
    fun withBlendMode(_blendMode: BlendMode): AnimationFrameInfo {
        return if (blendMode == _blendMode) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            isFullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            _blendMode,
            frameRect
        )
    }

    /**
     *
     * The rectangle updated by this frame.
     *
     *
     * It may be empty, if the frame does not change the image. It will
     * always be contained by [Codec.getSize].
     * @return `this`.
     */
    fun withFrameRect(_frameRect: IRect): AnimationFrameInfo {
        return if (frameRect === _frameRect) this else AnimationFrameInfo(
            requiredFrame,
            duration,
            isFullyReceived,
            alphaType,
            isHasAlphaWithinBounds,
            disposalMethod,
            blendMode,
            _frameRect
        )
    }
}