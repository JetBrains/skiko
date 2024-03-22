package org.jetbrains.skiko.sample

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate

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

        val skiaLayer = SkiaLayer()
        skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, Clocks(skiaLayer, holder))
        skiaLayer.attachTo(holder)
        layout.addView(holder)

        setContentView(layout, layout.layoutParams)
    }
}
