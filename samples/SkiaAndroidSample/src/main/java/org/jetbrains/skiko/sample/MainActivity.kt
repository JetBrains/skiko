package org.jetbrains.skiko.sample

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import org.jetbrains.skia.impl.Log
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.Version

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.debug("onCreate: ${Version.skia}")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val holder = LinearLayout(this).apply {
            //layoutParams = ViewGroup.LayoutParams(1000, 1200)
        }
        val skiaLayer = SkiaLayer()
        skiaLayer.skikoView = GenericSkikoView(skiaLayer, RotatingSquare())
        skiaLayer.attachTo(holder)
        layout.addView(holder)

        /*
        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        button.text = "Hello"
        layout.addView(button)
        */
        setContentView(layout, layout.layoutParams)
    }
}
