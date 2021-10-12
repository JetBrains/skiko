// Utils, that are not needed in scripts should go to internal/utils

fun joinToTitleCamelCase(vararg parts: String): String =
    parts.joinToString(separator = "") { part ->
        if (part.isEmpty()) part
        else buildString {
            append(part.first().toTitleCase())
            append(part.substring(1))
        }
    }