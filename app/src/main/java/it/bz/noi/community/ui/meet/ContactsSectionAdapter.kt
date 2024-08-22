// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.databinding.VhContactBinding
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.ui.HeaderViewHolder

private const val CONTACT = 0
private const val HEADER = 4

class ContactsSectionAdapter(
	private val initial: Char,
	private val detailListener: ContactDetailListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	init {
	    setHasStableIds(true)
	}

	constructor(initial: Char, contacs: List<Contact>, detailListener: ContactDetailListener) : this(initial, detailListener) {
		this.contacts = contacs
	}

	private var contacts: List<Contact> = emptyList()

	fun updateContacts(contacts: List<Contact>) {
		this.contacts = contacts
		notifyDataSetChanged()
	}

	override fun getItemCount(): Int  = contacts.size.let {
		if (it == 0) 0 else it + 1
	}

	override fun getItemViewType(position: Int): Int = when (position) {
		0 -> HEADER
		else -> CONTACT
	}

	override fun getItemId(position: Int): Long {
		return when (val viewType = getItemViewType(position)) {
			HEADER -> initial.code.toLong()
			CONTACT -> viewType.toLong() * 33 + contacts[position - 1].id.hashCode()
			else -> throw UnkownViewTypeException(viewType)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			CONTACT -> ContactVH(
				VhContactBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				), detailListener
			)
			HEADER -> HeaderViewHolder(VhHeaderBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.vh_header, parent, false)))
			else -> throw UnkownViewTypeException(viewType)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		Log.d("AdapterDebug", "Binding item at position: " + position + ", itemViewType: " + getItemViewType(position));
		when (holder) {
			is ContactVH -> holder.bind(contacts[position - 1])
			is HeaderViewHolder -> holder.bind("$initial")
		}
	}
}
