// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.databinding.VhContactBinding
import it.bz.noi.community.databinding.VhEmptyBinding

private const val TAG = "ContactsAdapter"
private const val CONTACT = 0
private const val LOADING = 1
private const val EMPTY = 3

/**
 * Adapter for the contacts list with just the loading item.
 */
class ContactsLoadingOrEmptyAdapter(state: State) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	enum class State {
		Loading,
		Empty
	}

	var state: State = state
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	override fun getItemViewType(position: Int): Int {
		return when (state) {
			State.Loading -> LOADING
			State.Empty -> EMPTY
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			LOADING -> EmptyViewHolder(
				VhEmptyBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				).apply {
					title.isVisible = false
					subtitle.isVisible = false
				}
			)
			EMPTY -> EmptyViewHolder(
				VhEmptyBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				).apply {
					subtitle.text = parent.resources.getString(R.string.label_contacts_empty_state_subtitle)
				}
			)
			else -> throw UnkownViewTypeException(viewType)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		// nothing to do
		Log.d("AdapterDebug", "Binding item at position: " + position + ", itemViewType: " + getItemViewType(position));
	}

	override fun getItemCount(): Int = 1
}

@Deprecated("Concatenate ContactsSectionAdapter instead")
class ContactsAdapter(private val detailListener: ContactDetailListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	sealed class AdapterState {
		data class Ok(val contacts: List<Contact> = emptyList()) : AdapterState()
		object Loading : AdapterState()
	}

	sealed class Item(val viewType: Int) {
		object Loading: Item(LOADING)
		object Empty: Item(EMPTY)
		data class Contact(val contact: it.bz.noi.community.data.models.Contact) : Item(CONTACT)
	}

	var state: AdapterState = AdapterState.Loading
		set(value) {
			if (field != value) {
				field = value
				items = value.toItems()
			}
		}

	private var items: List<Item> = listOf(Item.Loading)
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	private fun AdapterState.toItems(): List<Item> {
		return when (this) {
			is AdapterState.Ok -> when (contacts.size) {
				0 -> listOf(Item.Empty)
				else -> contacts.map { c ->
					Item.Contact(c)
				}
			}
			is AdapterState.Loading -> listOf(Item.Loading)
		}
	}

	override fun getItemViewType(position: Int): Int = items[position].viewType

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			CONTACT -> ContactVH(
                VhContactBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), detailListener
            )
			LOADING -> EmptyViewHolder(
                VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                    title.isVisible = false
                    subtitle.isVisible = false
                }
            )
			EMPTY -> EmptyViewHolder(
                VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                    subtitle.text =
                        parent.resources.getString(R.string.label_contacts_empty_state_subtitle)
                }
            )
			else -> throw UnkownViewTypeException(viewType)
		}
	}


	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		Log.d("AdapterDebug", "Binding item at position: " + position + ", itemViewType: " + getItemViewType(position));
		when (holder.itemViewType) {
			CONTACT -> {
				(items[position] as Item.Contact).let { txItem ->
					(holder as ContactVH).bind(txItem.contact)
				}
			}
		}
	}

	override fun getItemCount(): Int = items.size
}
