package it.bz.noi.community.data.models

data class UrlParams(
    var startDate: String,
    var endDate: String? = null,

    var public: Boolean = false,
    var noiOnly: Boolean = false,

    var green: Boolean = false,
    var food: Boolean = false,
    var digital: Boolean = false,
    var automotiv: Boolean = false,
)

fun UrlParams.getEventTypeRawFilter(): String? {
    var rawFilter: String? = null
    if (public || noiOnly) {
        rawFilter = "in(CustomTagging.[],"
        // TODO if (public) rawFilter = rawFilter.plus("'',")
        if (noiOnly) rawFilter = rawFilter.plus("\"Summer at NOI\",")
        rawFilter = rawFilter.substring(0, rawFilter.length-1).plus(")")
    }
    return rawFilter
}

fun UrlParams.getRawFilter(): String? {
    var rawFilter: String? = null
    if (green || food || digital || automotiv) {
        rawFilter = "in(TechnologyFields.[],"
        if (green) rawFilter = rawFilter.plus("\"Green\",")
        if (food) rawFilter = rawFilter.plus("'Food',")
        if (digital) rawFilter = rawFilter.plus("'Digital',")
        if (automotiv) rawFilter = rawFilter.plus("'Automotiv',")
        rawFilter = rawFilter.substring(0, rawFilter.length-1).plus(")")
    }
    return rawFilter
}

fun UrlParams.resetFilters() {
    public = false
    noiOnly = false
    green = false
    food = false
    digital = false
    automotiv = false
}