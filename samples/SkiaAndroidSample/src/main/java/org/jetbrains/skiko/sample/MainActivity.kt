package org.jetbrains.skiko.sample

import android.os.Bundle
import android.view.SurfaceView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.skia.impl.Log
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.Version

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.debug("onCreate: ${Version.skia}")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val skiaLayer = SkiaLayer()
        skiaLayer.skikoView = GenericSkikoView(skiaLayer, RotatingSquare())
        skiaLayer.attachTo(layout)

        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        button.text = "Hello"
        layout.addView(button)


        setContentView(layout, layout.layoutParams)
    }
}
