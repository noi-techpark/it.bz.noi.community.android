package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentMeetBinding
import it.bz.noi.community.databinding.FragmentNewsDetailsBinding
import it.bz.noi.community.databinding.VhContactBinding
import it.bz.noi.community.databinding.ViewHolderNewsBinding
import it.bz.noi.community.oauth.AccountsManager
import it.bz.noi.community.oauth.AuthManager.application
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.SimpleListAdapter
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.newsDetails.NewsDetailViewModelFactory
import it.bz.noi.community.ui.today.NewsVH
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers

class MeetFragment : Fragment() {

	private var _binding: FragmentMeetBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MeetViewModel by viewModels(factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService))
	})

	private lateinit var contactsAdapter: ContactsAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		contactsAdapter = ContactsAdapter(object : ContactDetailListener {
			override fun openContactDetail(contact: Contact) {
				// TODO
				Toast.makeText(requireContext(), "Dettaglio contatto ${contact.id}", Toast.LENGTH_SHORT).show()
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


		viewModel.contactsFlow.asLiveData(Dispatchers.Main).observe(viewLifecycleOwner) { res ->
			when (res.status) {
				Status.SUCCESS -> {
					val contacts = res.data!!
					Log.d(TAG, "Caricati ${contacts.size} contatti")
					val availableCompanies = AccountsManager.availableCompanies.value
					contactsAdapter.items = contacts.map { c ->
						if (c.accountId != null) {
							c.copy(companyName = availableCompanies.get(c.accountId)?.name)
						} else {
							c
						}
					}
					binding.swipeRefreshContacts.isRefreshing = false
				}
				Status.ERROR -> {
					contactsAdapter.items = emptyList()
					Log.d(TAG, "Caricamento contatti KO")
					binding.swipeRefreshContacts.isRefreshing = false
					Toast.makeText(requireContext(), res.message, Toast.LENGTH_LONG).show()
				}
				Status.LOADING -> {
					contactsAdapter.items = emptyList()
					Log.d(TAG, "Contatti in caricamento...")
					binding.swipeRefreshContacts.isRefreshing = true
				}
			}
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
		binding.contactIcon.text = "" + c.firstName[0] + c.lastName[0]
	}

}
