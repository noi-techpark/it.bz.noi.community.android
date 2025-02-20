// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import it.bz.noi.community.data.models.AccountType

/**
 * The category filter for the meet filters.
 */
enum class CategoryFilter(val types: List<AccountType>) {
    ALL(listOf()),
    COMPANY(listOf(AccountType.COMPANY)),
    STARTUP(listOf(AccountType.STARTUP)),
    RESEARCH_INSTITUTION(listOf(AccountType.RESEARCH_INSTITUTION))
}
