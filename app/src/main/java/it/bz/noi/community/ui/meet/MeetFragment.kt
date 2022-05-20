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

class MeetFragment : Fragment() {

	private var _binding: FragmentMeetBinding? = null
	private val binding get() = _binding!!

/*	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), JsonFilterRepository(application))
	})*/

	private val viewModel: MeetViewModel by viewModels(factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService),
			this@MeetFragment)
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

		viewModel.getContacts(AccountsManager.availableCompanies.value!!).observe(viewLifecycleOwner) {
			contactsAdapter.items = it
			binding.swipeRefreshContacts.isRefreshing = false
		}

		binding.swipeRefreshContacts.setOnRefreshListener {
			viewModel.refreshContacts()
		}

/*		// FIXME
		mainViewModel.availableContacts.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				Log.d(TAG, "Elenco contatti caricato")
			}
		}*/
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
