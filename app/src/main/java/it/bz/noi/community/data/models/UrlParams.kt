package it.bz.noi.community.data.models

data class UrlParams(
    var startDate: String,
    var endDate: String? = null,

	var filters: List<FilterValue> = emptyList()
)

private fun UrlParams.getEventTypeRawFilter(): String? {
    var rawFilter: String?

	val rawFiltersList = mutableListOf<String>()
	filters.filter { it.type == FilterType.EVENT_TYPE.typeDesc }.forEach {
		rawFiltersList.add("in(${FilterType.EVENT_TYPE.typeDesc}.[],\"${it.key}\")")
	}

	if (rawFiltersList.isEmpty())
		return null

	if (rawFiltersList.size == 1) {
		rawFilter = rawFiltersList[0]
	} else {
		rawFilter = "or(" // FIXME giusto OR?
		rawFiltersList.forEach {
			rawFilter += it
			rawFilter += ","
		}
		rawFilter = rawFilter.substring(0, rawFilter.lastIndexOf(",")) + ")"
	}

    return rawFilter
}

private fun UrlParams.getTechSectorRawFilter(): String? {
	var rawFilter: String?

	val rawFiltersList = mutableListOf<String>()
	filters.filter { it.type == FilterType.TECHNOLOGY_SECTOR.typeDesc }.forEach {
		rawFiltersList.add("in(${FilterType.TECHNOLOGY_SECTOR.typeDesc}.[],\"${it.key}\")")
	}

	if (rawFiltersList.isEmpty())
		return null

	if (rawFiltersList.size == 1) {
		rawFilter = rawFiltersList[0]
	} else {
		rawFilter = "or(" // FIXME giusto OR?
		rawFiltersList.forEach {
			rawFilter += it
			rawFilter += ","
		}
		rawFilter = rawFilter.substring(0, rawFilter.lastIndexOf(",")) + ")"
	}

	return rawFilter
}

fun UrlParams.getRawFilter(): String? {
	if (filters == null || filters.isEmpty())
		return null

    val eventTypeRawFilter = getEventTypeRawFilter()
    val techSectorRawFilter = getTechSectorRawFilter()

    var rawFilter: String? = null
    if (eventTypeRawFilter != null && techSectorRawFilter != null)
        rawFilter = "and(".plus(eventTypeRawFilter).plus(",").plus(techSectorRawFilter).plus(")") // FIXME giusto AND?
    else if (eventTypeRawFilter != null)
        rawFilter = eventTypeRawFilter
    else if (techSectorRawFilter != null)
        rawFilter = techSectorRawFilter
    return rawFilter
}
