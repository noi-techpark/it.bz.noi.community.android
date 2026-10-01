// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

data class EventsParams(
    var startDate: String,
    var endDate: String? = null,

	var selectedFilters: List<FilterValue> = emptyList()
)

private const val NOI_LOCATION_TAG_ID = "noi"

private fun tagRawFilter(tagId: String) = "in(TagIds.[],\"$tagId\")"

/*
 * Filtri per tipo evento:
 * - sono mutualmente esclusivi
 * - c'è un singolo filtro attivo e quando uno nuovo viene attivato si spengono gli altri.
 * - Noi li implementiamo forgiando una query solo con solo un filtro attivo senza nessuna OR/AND
 */
private fun EventsParams.getEventTypeRawFilter(): String? {
    var rawFilter: String?

	val rawFiltersList = selectedFilters.filter { it.type == EventsFilterType.EVENT_TYPE.category }.map {
		tagRawFilter(it.key)
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

	val rawFiltersList = selectedFilters.filter { it.type == EventsFilterType.TECHNOLOGY_SECTOR.category }.map {
		tagRawFilter(it.key)
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
 * Filtro complessivo.
 *
 * La location NOI non è più un parametro di query dedicato nella nuova API: è
 * solo un altro tag, quindi va sempre incluso come componente AND, insieme agli
 * eventuali filtri selezionati dall'utente.
 */
fun EventsParams.getRawFilter(): String {
    val components = listOfNotNull(
		tagRawFilter(NOI_LOCATION_TAG_ID),
		getEventTypeRawFilter(),
		getTechSectorRawFilter(),
	)

	return if (components.size == 1)
		components[0]
	else
		components.joinToString(prefix = "and(", separator = ",", postfix = ")")
}
