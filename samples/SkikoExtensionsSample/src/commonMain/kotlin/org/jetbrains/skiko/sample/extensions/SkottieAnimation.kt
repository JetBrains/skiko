package org.jetbrains.skiko.sample.extensions

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import kotlin.math.min
import kotlin.time.TimeSource

internal const val APP_NAME = "Skiko Extensions Sample"
internal const val RENDER_SUCCESS_MESSAGE = "Skiko extensions sample rendered bundled Skottie animation successfully"

internal fun exitAfterMillis(args: Array<String>): Int? =
    args.firstNotNullOfOrNull { argument ->
        argument.removePrefix("--exit-after-ms=").takeIf { it != argument }?.toInt()
    }

fun makeSkottieAnimationPlayer(animationJson: String): SkottieAnimationPlayer =
    SkottieAnimationPlayer(Animation.makeFromString(animationJson))

internal fun loadSkottieAnimationPlayer(): SkottieAnimationPlayer =
    makeSkottieAnimationPlayer(ORBIT_ANIMATION_JSON)

class SkottieAnimationPlayer(
    private val animation: Animation
) {
    private val startedAt = TimeSource.Monotonic.markNow()

    fun render(canvas: Canvas, width: Int, height: Int) {
        canvas.clear(Color.WHITE)

        val duration = animation.duration.coerceAtLeast(1f)
        val elapsedSeconds = startedAt.elapsedNow().inWholeNanoseconds / 1_000_000_000.0f
        val progress = (elapsedSeconds % duration) / duration
        animation.seek(progress)

        val animationWidth = animation.size.x
        val animationHeight = animation.size.y
        val scale = min(width / animationWidth, height / animationHeight) * 0.85f
        val scaledWidth = animationWidth * scale
        val scaledHeight = animationHeight * scale
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f

        animation.render(
            canvas,
            Rect.makeXYWH(left, top, scaledWidth, scaledHeight)
        )
    }
}
