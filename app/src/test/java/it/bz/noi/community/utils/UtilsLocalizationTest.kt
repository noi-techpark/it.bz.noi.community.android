// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class UtilsLocalizationTest {

	@Test
	fun `returns the device language when present`() {
		val default = Locale.getDefault()
		Locale.setDefault(Locale.ITALIAN)
		try {
			assertEquals("ciao", Utils.localizedOrFirst(mapOf("en" to "hello", "it" to "ciao")))
		} finally {
			Locale.setDefault(default)
		}
	}

	@Test
	fun `falls back to english when device language is absent`() {
		val default = Locale.getDefault()
		Locale.setDefault(Locale.FRENCH)
		try {
			assertEquals("hello", Utils.localizedOrFirst(mapOf("en" to "hello", "it" to "ciao")))
		} finally {
			Locale.setDefault(default)
		}
	}

	@Test
	fun `falls back to whatever language is present when neither device language nor english are available`() {
		val default = Locale.getDefault()
		Locale.setDefault(Locale.FRENCH)
		try {
			assertEquals("hallo", Utils.localizedOrFirst(mapOf("de" to "hallo")))
		} finally {
			Locale.setDefault(default)
		}
	}

	@Test
	fun `returns null for an empty or null map`() {
		assertNull(Utils.localizedOrFirst(emptyMap<String, String>()))
		assertNull(Utils.localizedOrFirst<String>(null))
	}
}
