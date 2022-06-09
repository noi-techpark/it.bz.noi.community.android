package it.bz.noi.community.data.models

data class EventsParams(
    var startDate: String,
    var endDate: String? = null,

	var selectedFilters: List<FilterValue> = emptyList()
)

/*
 * Filtri per tipo evento:
 * - sono mutualmente esclusivi
 * - c'è un singolo filtro attivo e quando uno nuovo viene attivato si spengono gli altri.
 * - Noi li implementiamo forgiando una query solo con solo un filtro attivo senza nessuna OR/AND
 */
private fun EventsParams.getEventTypeRawFilter(): String? {
    var rawFilter: String?

	val rawFiltersList = selectedFilters.filter { it.type == EventsFilterType.EVENT_TYPE.typeDesc }.map {
		"in(${EventsFilterType.EVENT_TYPE.typeDesc}.[],\"${it.key}\")"
	}

	if (rawFiltersList.isEmpty())
		return null

	if (rawFiltersList.size == 1) {
			// Nella pratica, finiremo sempre in questo caso perchè i due filtri "Public" e "NOI-Only" sono mutuamente esclusivi,
			// quindi se ne potrà selezionare solo uno dei due
		rawFilter = rawFiltersList[0]
	} else {
		rawFilter = rawFiltersList.joinToString(prefix = "or(", separator = ",", postfix = ")")
	}

    return rawFilter
}

/*
 * Filtri per settore tecnologico:
 * - sono selezionabili più filtri
 * - sono unione di insieme (cioè il numero di risultati crescerà).
 * - Noi li implementiamo forgiando una query OR
 */
private fun EventsParams.getTechSectorRawFilter(): String? {
	var rawFilter: String?

	val rawFiltersList = selectedFilters.filter { it.type == EventsFilterType.TECHNOLOGY_SECTOR.typeDesc }.map {
		"in(${EventsFilterType.TECHNOLOGY_SECTOR.typeDesc}.[],\"${it.key}\")"
	}

	if (rawFiltersList.isEmpty())
		return null

	if (rawFiltersList.size == 1) {
		rawFilter = rawFiltersList[0]
	} else {
		rawFilter = rawFiltersList.joinToString(prefix = "or(", separator = ",", postfix = ")")
	}

	return rawFilter
}

/*
 * Filtro complessivo
 */
fun EventsParams.getRawFilter(): String? {
	if (selectedFilters == null || selectedFilters.isEmpty())
		return null

    val eventTypeRawFilter = getEventTypeRawFilter()
    val techSectorRawFilter = getTechSectorRawFilter()

    var rawFilter: String? = null
    if (eventTypeRawFilter != null && techSectorRawFilter != null)
    	// Filtro sia per evento sia per settore tecnologico: sono una intersezione di insiemi e quindi vanno messi in AND. Es eventi pubblici e del settore green
        rawFilter = "and(".plus(eventTypeRawFilter).plus(",").plus(techSectorRawFilter).plus(")")
    else if (eventTypeRawFilter != null)
        rawFilter = eventTypeRawFilter
    else if (techSectorRawFilter != null)
        rawFilter = techSectorRawFilter
    return rawFilter
}
