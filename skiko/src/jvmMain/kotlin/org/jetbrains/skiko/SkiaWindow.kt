package org.jetbrains.skiko

import org.jetbrains.skija.Library

class SkiaWindow {
  companion object {
    init {
      Library.load()
    }
  }

  external fun nativeMethod(param: Long): Long
}

