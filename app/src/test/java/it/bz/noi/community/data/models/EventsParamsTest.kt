// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class EventsParamsTest {

	private fun paramsWith(vararg filters: FilterValue) =
		EventsParams(startDate = "2026-01-01 00:00", selectedFilters = filters.toList())

	@Test
	fun `no filters selected only includes the noi location predicate`() {
		val params = paramsWith()
		assertEquals("""in(TagIds.[],"noi")""", params.getRawFilter())
	}

	@Test
	fun `one event-type filter is anded with location`() {
		val params = paramsWith(FilterValue(key = "sport", type = EventsFilterType.EVENT_TYPE.category, desc = "Sport"))
		assertEquals(
			"""and(in(TagIds.[],"noi"),in(TagIds.[],"sport"))""",
			params.getRawFilter()
		)
	}

	@Test
	fun `two technology-sector filters are ored then anded with location`() {
		val params = paramsWith(
			FilterValue(key = "digital", type = EventsFilterType.TECHNOLOGY_SECTOR.category, desc = "Digital"),
			FilterValue(key = "green", type = EventsFilterType.TECHNOLOGY_SECTOR.category, desc = "Green"),
		)
		assertEquals(
			"""and(in(TagIds.[],"noi"),or(in(TagIds.[],"digital"),in(TagIds.[],"green")))""",
			params.getRawFilter()
		)
	}

	@Test
	fun `one filter from each group anded with location as three components`() {
		val params = paramsWith(
			FilterValue(key = "sport", type = EventsFilterType.EVENT_TYPE.category, desc = "Sport"),
			FilterValue(key = "digital", type = EventsFilterType.TECHNOLOGY_SECTOR.category, desc = "Digital"),
		)
		assertEquals(
			"""and(in(TagIds.[],"noi"),in(TagIds.[],"sport"),in(TagIds.[],"digital"))""",
			params.getRawFilter()
		)
	}
}
