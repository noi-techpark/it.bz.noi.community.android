// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import it.bz.noi.community.data.models.Contact

interface ContactDetailListener {
	fun openContactDetail(contact: Contact)
}
