package org.jetbrains.skiko.sample

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoGestureEventKind

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val holder = LinearLayout(this).apply {
            //layoutParams = ViewGroup.LayoutParams(1000, 1200)
        }

        val skiaLayer = SkiaLayer(SkikoGestureEventKind.values())
        skiaLayer.skikoView = GenericSkikoView(skiaLayer, Clocks(skiaLayer))
        skiaLayer.attachTo(holder)
        layout.addView(holder)

        setContentView(layout, layout.layoutParams)
    }
}
