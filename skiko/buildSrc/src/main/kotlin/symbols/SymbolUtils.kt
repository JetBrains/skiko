package symbols

internal fun isOrgJetbrainsSymbol(name: String): Boolean =
    name.removePrefix("_").startsWith("org_jetbrains")

internal fun isJniInfrastructureSymbol(name: String): Boolean {
    val symbol = name.removePrefix("_")

    return symbol.startsWith("Java_") ||
            symbol.startsWith("JNI") ||
            symbol.startsWith("jvm")
}
