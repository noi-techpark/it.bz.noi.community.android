// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.databinding.VhContactBinding

class ContactVH(private val binding: VhContactBinding, detailListener: ContactDetailListener) : RecyclerView.ViewHolder(binding.root) {

	private lateinit var contact: Contact

	init {
		binding.root.setOnClickListener {
			detailListener.openContactDetail(contact)
		}
	}

	fun bind(c: Contact) {
		contact = c

		binding.contactName.text = c.fullName
		binding.companyName.text = c.companyName
		binding.companyName.isVisible = c.companyName != null
		binding.contactIcon.text = "${c.firstName[0]}${c.lastName[0]}"
	}

}
