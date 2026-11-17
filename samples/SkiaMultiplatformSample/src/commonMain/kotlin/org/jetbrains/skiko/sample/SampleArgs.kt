package org.jetbrains.skiko.sample

internal fun exitAfterMillis(args: Array<String>): Int? =
    args.firstNotNullOfOrNull { argument ->
        argument.removePrefix("--exit-after-ms=").takeIf { it != argument }?.toInt()
    }
