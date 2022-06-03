package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
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
					contactsAdapter.items = contacts
					binding.swipeRefreshContacts.isRefreshing = false
				}
				Status.ERROR -> {
					contactsAdapter.items = emptyList()
					Log.d(TAG, "Caricamento contatti KO")
					binding.swipeRefreshContacts.isRefreshing = false
					Toast.makeText(requireContext(), res.message, Toast.LENGTH_LONG).show()
				}
				Status.LOADING -> {
					Log.d(TAG, "Contatti in caricamento...")
					binding.swipeRefreshContacts.isRefreshing = true
				}
			}
		}

		binding.searchFieldEditText.doOnTextChanged { text, _, _, _ ->
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

class ContactsAdapter(private val detailListener: ContactDetailListener) : RecyclerView.Adapter<ContactVH>() {

	var items = emptyList<Contact>()
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactVH {
		return ContactVH(VhContactBinding.inflate(LayoutInflater.from(parent.context), parent, false), detailListener)
	}

	override fun onBindViewHolder(holder: ContactVH, position: Int) {
		items.get(position)?.let {
			holder.bind(it)
		}
	}

	override fun getItemCount(): Int = items.size
}

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
