package org.jetbrains.skiko

class SkiaWindow {
  companion object {
    init {
      Library.load("/", "skiko")
    }
  }

  external fun nativeMethod(param: Long): Long
}

