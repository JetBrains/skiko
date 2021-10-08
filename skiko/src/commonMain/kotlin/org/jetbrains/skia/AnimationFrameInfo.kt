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
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is AnimationFrameInfo) return false
        if (requiredFrame != other.requiredFrame) return false
        if (duration != other.duration) return false
        if (isFullyReceived != other.isFullyReceived) return false
        if (isHasAlphaWithinBounds != other.isHasAlphaWithinBounds) return false
        if (this.alphaType != other.alphaType) return false
        if (this.disposalMethod != other.disposalMethod) return false
        if (this.blendMode != other.blendMode) return false
        return this.frameRect == other.frameRect
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + requiredFrame
        result = result * PRIME + duration
        result = result * PRIME + if (isFullyReceived) 79 else 97
        result = result * PRIME + if (isHasAlphaWithinBounds) 79 else 97
        result = result * PRIME + alphaType.hashCode()
        result = result * PRIME + disposalMethod.hashCode()
        result = result * PRIME + blendMode.hashCode()
        result = result * PRIME + frameRect.hashCode()
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