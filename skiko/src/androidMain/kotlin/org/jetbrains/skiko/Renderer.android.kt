package org.jetbrains.skiko

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.widget.LinearLayout
import android.app.Activity
import android.view.MotionEvent
import android.view.KeyEvent
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.*
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkikoSurfaceView(context: Context, val layer: SkiaLayer) : GLSurfaceView(context) {
    private val renderer = SkikoSurfaceRender(layer)
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (layer.gestures != null) {
            setOnTouchListener { _, event ->
                if (layer.gestures.contains(SkikoGestureEventKind.PINCH)) {
                    scaleGesture.onTouchEvent(event)
                }
                if (layer.gestures.contains(SkikoGestureEventKind.ROTATION)) {
                    rotationGesture.onTouchEvent(event)
                }
                simpleGestures.onTouchEvent(event)
            }
        }
        setEGLConfigChooser (8, 8, 8, 0, 24, 8)
        setEGLContextClientVersion(2)
        // setRenderMode(RENDERMODE_WHEN_DIRTY)
        setRenderer(renderer)
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Main) {
        renderer.update()
        requestRender()
    }

    fun scheduleFrame() {
        frameDispatcher.scheduleFrame()
    }

    private val simpleGestures = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (!layer.gestures!!.contains(SkikoGestureEventKind.TAP)) return false
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
            if (!layer.gestures!!.contains(SkikoGestureEventKind.DOUBLETAP)) return false
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
            if (!layer.gestures!!.contains(SkikoGestureEventKind.LONGPRESS)) return
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

        override fun onScroll(
            event1: MotionEvent,
            event2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            if (!layer.gestures!!.contains(SkikoGestureEventKind.PAN)) return false
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

        override fun onFling(
            event1: MotionEvent, 
            event2: MotionEvent, 
            velocityX: Float, 
            velocityY: Float
        ): Boolean {
            if (!layer.gestures!!.contains(SkikoGestureEventKind.SWIPE)) return false
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

    private val scaleGesture = SkikoScaleGestureDetector(context, SkikoScaleGestureListener(layer))

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

    private val rotationGesture = SkikoRotationGestureDetector(SkikoRotationGestureListener(layer))

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val events: MutableList<SkikoTouchEvent> = mutableListOf()
        val count = event.pointerCount
        for (index in 0 .. count - 1) {
            events.add(toSkikoTouchEvent(event, index, layer.contentScale))
        }
        layer.skikoView?.onTouchEvent(events.toTypedArray())
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        layer.skikoView?.onKeyboardEvent(
            toSkikoKeyboardEvent(event, keyCode, SkikoKeyboardEventKind.DOWN)
        )
        if (event.unicodeChar != 0) {
            layer.skikoView?.onInputEvent(
                toSkikoTypeEvent(event, keyCode)
            )
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        layer.skikoView?.onKeyboardEvent(
            toSkikoKeyboardEvent(event, keyCode, SkikoKeyboardEventKind.UP)
        )
        return true
    }
}

private class SkikoSurfaceRender(private val layer: SkiaLayer) : GLSurfaceView.Renderer {
    private var width: Int = 0
    private var height: Int = 0

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    private fun <T : Any> lockPicture(action: (PictureHolder) -> T): T? {
        return synchronized(pictureLock) {
            val picture = picture
            if (picture != null) {
                action(picture)
            } else {
                null
            }
        }
    }

    // This method is called from the main thread.
    fun update() {
        layer.skikoView?.let {
            val bounds = Rect.makeWH(width.toFloat(), width.toFloat())
            val canvas = pictureRecorder.beginRecording(bounds)
            try {
                it.onRender(canvas, width, height, System.nanoTime())
            } finally {
                synchronized(pictureLock) {
                    picture?.instance?.close()
                    val picture = pictureRecorder.finishRecordingAsPicture()
                    this.picture = PictureHolder(picture, width, height)
                }
            }
        }
    }

    // This method is called from GL rendering thread.
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!
        gl.glClearColor(0f, 0f, 0f, 0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    // This method is called from GL rendering thread.
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl!!
        this.width = width
        this.height = height
        initCanvas(gl)
    }

    // This method is called from GL rendering thread, it shall render Skia picture.
    override fun onDrawFrame(gl: GL10?) {
        lockPicture {
            canvas?.clear(-1)
            canvas?.drawPicture(it.instance)
            Unit
        }

        context?.flush()
    }

    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

    private fun initCanvas(gl: GL10) {
        disposeCanvas()
        val intBuf1 = IntBuffer.allocate(1)
        gl.glGetIntegerv(GLES30.GL_DRAW_FRAMEBUFFER_BINDING, intBuf1)
        val fbId = intBuf1[0]
        renderTarget = makeGLRenderTarget(
            width,
            height,
            0,
            8,
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        )
        context = makeGLContext()
        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )
        canvas = surface!!.canvas
    }

    private fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }
}
