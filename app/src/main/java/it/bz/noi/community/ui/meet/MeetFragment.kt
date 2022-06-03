package it.bz.noi.community.ui.meet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.databinding.FragmentMeetBinding
import it.bz.noi.community.databinding.VhContactBinding
import it.bz.noi.community.databinding.VhEmptyBinding
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers

class MeetFragment : Fragment() {

	private var _binding: FragmentMeetBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MeetViewModel by viewModels(factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), this)
	})

	private lateinit var contactsAdapter: ContactsAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		contactsAdapter = ContactsAdapter(object : ContactDetailListener {
			override fun openContactDetail(contact: Contact) {
				findNavController().navigate(MeetFragmentDirections.actionToContactDetails(contact))
			}
		})
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMeetBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.contacts.adapter = contactsAdapter

		binding.swipeRefreshContacts.setOnRefreshListener {
			viewModel.refreshContacts()
		}

		viewModel.filteredContactsFlow.asLiveData(Dispatchers.Main).observe(viewLifecycleOwner) { res ->
			when (res.status) {
				Status.SUCCESS -> {
					val contacts = res.data!!
					Log.d(TAG, "Caricati ${contacts.size} contatti")
					contactsAdapter.state = ContactsAdapter.AdapterState.Ok(contacts)
					binding.swipeRefreshContacts.isRefreshing = false
				}
				Status.ERROR -> {
					Log.d(TAG, "Caricamento contatti KO")
					binding.swipeRefreshContacts.isRefreshing = false
					// FIXME gestire uno state nell'adapter??
					Toast.makeText(requireContext(), res.message, Toast.LENGTH_LONG).show()
				}
				Status.LOADING -> {
					Log.d(TAG, "Contatti in caricamento...")
					binding.swipeRefreshContacts.isRefreshing = true
				}
			}
		}

		binding.searchFieldEditText.addTextChangedListener { text ->
			viewModel.updateSearchParam(text)
		}

	}

	companion object {
		private const val TAG = "MeetFragment"
	}

}

interface ContactDetailListener {
	fun openContactDetail(contact: Contact)
}

class ContactsAdapter(private val detailListener: ContactDetailListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	companion object {
		const val TAG = "ContactsAdapter"
		const val CONTACT = 0
		const val LOADING = 1
		const val EMPTY = 3
	}


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
			CONTACT -> ContactVH(VhContactBinding.inflate(LayoutInflater.from(parent.context), parent, false), detailListener)
			LOADING -> EmptyViewHolder(
				VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
					title.isVisible = false
					subtitle.isVisible = false
				}
			)
			EMPTY -> EmptyViewHolder(
				VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			)
			else -> throw UnkownViewTypeException(viewType)
		}
	}


	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

class UnkownViewTypeException(id: Int) : Exception("Unkown view type $id")

class EmptyViewHolder(binding: VhEmptyBinding) : RecyclerView.ViewHolder(binding.root)

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
