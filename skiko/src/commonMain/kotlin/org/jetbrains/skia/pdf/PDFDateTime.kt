package org.jetbrains.skia.pdf

/*
 * This class mirrors SkPDF::DateTime, but as Skia uses it only in the PDF backend,
 * we've moved it into the PDF package to not pollute the main namespace.
 *
 * Notice that we have omitted the dayOfWeek field, as it is unused in the PDF backend.
 */
/**
 * @property year Year, e.g., 2023.
 * @property month Month between 1 and 12.
 * @property day Day between 1 and 31.
 * @property hour Hour between 0 and 23.
 * @property minute Minute between 0 and 59.
 * @property second Second between 0 and 59.
 * @property timeZoneMinutes The number of minutes that the time zone is ahead of or behind UTC.
 */
data class PDFDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val timeZoneMinutes: Int = 0
) {

    init {
        require(month in 1..12) { "Month must be between 1 and 12." }
        require(day in 1..31) { "Day must be between 1 and 31." }
        require(hour in 0..23) { "Hour must be between 0 and 23." }
        require(minute in 0..59) { "Minute must be between 0 and 59." }
        require(second in 0..59) { "Second must be between 0 and 59." }
    }

    internal fun asArray() = intArrayOf(year, month, day, hour, minute, second, timeZoneMinutes)

}