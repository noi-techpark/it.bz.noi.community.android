// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.bz.noi.community.utils.Utils.copyToClipboard

// BroadcastReceiver per gestire l'azione di copia
class CopyBroadcastReceiver : BroadcastReceiver() {
	companion object {
		const val CONTACT_INFO = "CONTACT_INFO"
	}

	override fun onReceive(context: Context, intent: Intent) {
		val textToCopy = intent.getStringExtra(CONTACT_INFO)
		if (textToCopy != null) {
			//TODO: non dovrebbe essere localizzata la label?
			context.copyToClipboard("contact_info_copied", textToCopy)
		}
	}
}
