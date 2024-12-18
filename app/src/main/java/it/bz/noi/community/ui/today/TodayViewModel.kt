// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today

import androidx.lifecycle.ViewModel
import it.bz.noi.community.data.models.Event

class TodayViewModel : ViewModel() {
	val events = arrayListOf<Event>()
}
