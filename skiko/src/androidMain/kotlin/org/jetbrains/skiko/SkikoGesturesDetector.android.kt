package org.jetbrains.skiko

import android.content.Context
import android.view.MotionEvent
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import org.jetbrains.skia.toRadians

internal class SkikoGesturesDetector(
    private val context: Context,
    private val layer: SkiaLayer
) {
    private var gesturesToListen: Array<SkikoGestureEventKind>? = null
    fun setGesturesToListen(gestures: Array<SkikoGestureEventKind>?) {
        gesturesToListen = gestures
    }

    private fun containsGesture(
        gesture: SkikoGestureEventKind
    ): Boolean {
        gesturesToListen?.let { list ->
            return list.contains(gesture)
        }
        return false
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (containsGesture(SkikoGestureEventKind.PINCH)) {
            scaleGesture.onTouchEvent(event)
        }
        if (containsGesture(SkikoGestureEventKind.ROTATION)) {
            rotationGesture.onTouchEvent(event)
        }
        return simpleGestures.onTouchEvent(event)
    }

    private val simpleGestures = GestureDetector(context, object: WorkaroundSimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (!containsGesture(SkikoGestureEventKind.TAP)) return false
            val density = layer.contentScale
            layer.skikoView?.onGestureEvent(
                SkikoGestureEvent(
                    x = (event.x / density).toDouble(),
                    y = (event.y / density).toDouble(),
                    kind = SkikoGestureEventKind.TAP,
                    platform = event
                )
            )
            return true
        }

        override fun onDoubleTap(event: MotionEvent): Boolean {
            if (!containsGesture(SkikoGestureEventKind.DOUBLETAP)) return false
            val density = layer.contentScale
            layer.skikoView?.onGestureEvent(
                SkikoGestureEvent(
                    x = (event.x / density).toDouble(),
                    y = (event.y / density).toDouble(),
                    kind = SkikoGestureEventKind.DOUBLETAP,
                    platform = event
                )
            )
            return true
        }

        override fun onLongPress(event: MotionEvent) {
            if (!containsGesture(SkikoGestureEventKind.LONGPRESS)) return
            val density = layer.contentScale
            layer.skikoView?.onGestureEvent(
                SkikoGestureEvent(
                    x = (event.x / density).toDouble(),
                    y = (event.y / density).toDouble(),
                    kind = SkikoGestureEventKind.LONGPRESS,
                    platform = event
                )
            )
        }

        override fun onScrollWorkaround(
            event1: MotionEvent?,
            event2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            if (!containsGesture(SkikoGestureEventKind.PAN)) return false
            val density = layer.contentScale
            layer.skikoView?.onGestureEvent(
                SkikoGestureEvent(
                    x = (event2.x / density).toDouble(),
                    y = (event2.y / density).toDouble(),
                    kind = SkikoGestureEventKind.PAN,
                    platform = event2
                )
            )
            return true
        }

        override fun onFlingWorkaround(
            event1: MotionEvent?,
            event2: MotionEvent, 
            velocityX: Float, 
            velocityY: Float
        ): Boolean {
            if (!containsGesture(SkikoGestureEventKind.SWIPE)) return false
            var direction = toSkikoGestureDirection(event1, event2, velocityX, velocityY)
            if (direction == SkikoGestureEventDirection.UNKNOWN) {
                return false
            }
            val density = layer.contentScale
            layer.skikoView?.onGestureEvent(
                SkikoGestureEvent(
                    x = (event2.x / density).toDouble(),
                    y = (event2.y / density).toDouble(),
                    direction = direction,
                    kind = SkikoGestureEventKind.SWIPE,
                    platform = event2
                )
            )
            return true
        }
    })

    private val scaleGesture = SkikoScaleGestureDetector(context, SkikoScaleGestureListener(layer))

    private class SkikoScaleGestureDetector(
        context: Context,
        private val scaleListener: SkikoScaleGestureListener
    ) : ScaleGestureDetector(context, scaleListener) {
        override fun onTouchEvent(event: MotionEvent): Boolean {
            scaleListener.event = event
            return super.onTouchEvent(event)
        }
    }

    private class SkikoScaleGestureListener(val layer: SkiaLayer) : SimpleOnScaleGestureListener() {
        var event: MotionEvent? = null
        var scale: Double = 1.0
        
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (event != null) {
                scale *= detector.scaleFactor.toDouble()
                layer.skikoView?.onGestureEvent(
                    toSkikoScaleGestureEvent(
                        event = event!!,
                        scale = scale,
                        state = SkikoGestureEventState.STARTED,
                        layer.contentScale
                    )
                )
                return true
            }
            return false
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (event != null) {
                scale *= detector.scaleFactor.toDouble()
                layer.skikoView?.onGestureEvent(
                    toSkikoScaleGestureEvent(
                        event = event!!,
                        scale = scale,
                        state = SkikoGestureEventState.CHANGED,
                        layer.contentScale
                    )
                )
                return true
            }
            return false
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (event != null) {
                scale *= detector.scaleFactor.toDouble()
                layer.skikoView?.onGestureEvent(
                    toSkikoScaleGestureEvent(
                        event = event!!,
                        scale = scale,
                        state = SkikoGestureEventState.ENDED,
                        layer.contentScale
                    )
                )
            }
            scale = 1.0
        }
    }

    private val rotationGesture = SkikoRotationGestureDetector(SkikoRotationGestureListener(layer))

    private class SkikoRotationGestureListener(val layer: SkiaLayer) {
            var event: MotionEvent? = null
            fun onRotation(detector: SkikoRotationGestureDetector) {
                if (event != null) {
                    val density = layer.contentScale
                    layer.skikoView?.onGestureEvent(
                        SkikoGestureEvent(
                            x = (event!!.x / density).toDouble(),
                            y = (event!!.y / density).toDouble(),
                            rotation = detector.angle,
                            kind = SkikoGestureEventKind.ROTATION,
                            state = SkikoGestureEventState.CHANGED,
                            platform = event
                        )
                    )
                }
            }
    }

    private class SkikoRotationGestureDetector(
        private val rotationListener: SkikoRotationGestureListener
    ) {
        private var fX = 0f
        private var fY = 0f
        private var sX = 0f
        private var sY = 0f
        private var ptrID1: Int
        private var ptrID2: Int
        var angle = 0.0
            private set

        companion object {
            private const val INVALID_POINTER_ID = -1
        }

        init {
            ptrID1 = INVALID_POINTER_ID
            ptrID2 = INVALID_POINTER_ID
        }

        fun onTouchEvent(event: MotionEvent): Boolean {
            rotationListener.event = event
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> ptrID1 = event.getPointerId(event.actionIndex)
                MotionEvent.ACTION_POINTER_DOWN -> {
                    ptrID2 = event.getPointerId(event.actionIndex)
                    sX = event.getX(event.findPointerIndex(ptrID1))
                    sY = event.getY(event.findPointerIndex(ptrID1))
                    fX = event.getX(event.findPointerIndex(ptrID2))
                    fY = event.getY(event.findPointerIndex(ptrID2))
                }
                MotionEvent.ACTION_MOVE -> if (ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID) {
                    val nfX: Float
                    val nfY: Float
                    val nsX: Float
                    val nsY: Float
                    nsX = event.getX(event.findPointerIndex(ptrID1))
                    nsY = event.getY(event.findPointerIndex(ptrID1))
                    nfX = event.getX(event.findPointerIndex(ptrID2))
                    nfY = event.getY(event.findPointerIndex(ptrID2))
                    angle = angleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY)
                    rotationListener.onRotation(this)
                }
                MotionEvent.ACTION_UP -> ptrID1 = INVALID_POINTER_ID
                MotionEvent.ACTION_POINTER_UP -> ptrID2 = INVALID_POINTER_ID
                MotionEvent.ACTION_CANCEL -> {
                    ptrID1 = INVALID_POINTER_ID
                    ptrID2 = INVALID_POINTER_ID
                }
            }
            return true
        }

        private fun angleBetweenLines(
            fX: Float,
            fY: Float,
            sX: Float,
            sY: Float,
            nfX: Float,
            nfY: Float,
            nsX: Float,
            nsY: Float
        ): Double {
            val angle1 = Math.atan2((fY - sY).toDouble(), (fX - sX).toDouble())
            val angle2 = Math.atan2((nfY - nsY).toDouble(), (nfX - nsX).toDouble())
            return (Math.toDegrees(angle2 - angle1) % 360).toFloat().toRadians()
        }
    }
}

typealias MotionEventNullableOrNo = MotionEvent?