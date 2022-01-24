package org.jetbrains.skiko.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.skia.impl.Log
import org.jetbrains.skiko.Version

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.debug("onCreate: ${Version.skia}")
    }
}
