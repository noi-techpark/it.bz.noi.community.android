// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.meet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.databinding.FragmentMeetBinding
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.Normalizer


@ExperimentalCoroutinesApi
class MeetFragment : Fragment() {

	private var _binding: FragmentMeetBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MeetViewModel by navGraphViewModels(R.id.navigation_meet, factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), this)
	})

	private lateinit var contactsAdapters: Map<Char,ContactsSectionAdapter>
	private lateinit var contactsAdapter: RecyclerView.Adapter<*>

	private val listener = object : ContactDetailListener {
		override fun openContactDetail(contact: Contact) {
			findNavController().navigate(MeetFragmentDirections.actionToContactDetails(contact))
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Define a map of contacts adapters for each alphabet letter
		contactsAdapters = (('A'..'Z').associateWith { ContactsSectionAdapter(listener) }) + ('#' to ContactsSectionAdapter(listener))
		contactsAdapter = ConcatAdapter(
			contactsAdapters.values.toList()
		)
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

		binding.apply {
			contacts.adapter = this@MeetFragment.contactsAdapter
			contacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {

				// Nasconde la tastiera, quando l'utente inizia a scrollare la lista
				override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
					super.onScrollStateChanged(recyclerView, newState)
					if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
						val imm: InputMethodManager =
							recyclerView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
						imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
					}
				}
			})

			swipeRefreshContacts.setOnRefreshListener {
				viewModel.refreshContacts()
			}

			searchFieldEditText.addTextChangedListener { text ->
				viewModel.updateSearchParam(text?.toString() ?: "")
			}

			contactsFilter.root.setOnClickListener {
				findNavController().navigate(MeetFragmentDirections.actionMeetToFilters())
			}

		}

		setupObservers()
	}

	/**
	 * Given a list of Contact, group them by the first letter of their first name.
	 * If there are not contacts for a given letter, use the empty list.
	 * Name starting not with a letter are grouped under the "#" key.
	 */
	private fun List<Contact>.groupedByFirstLetter(): Map<Char, List<Contact>> {
		fun String.removeAccents() = Normalizer.normalize(this, Normalizer.Form.NFD)
		fun String.firstLetterOrDefault(): Char = removeAccents().firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'
		val contactsByFirstLetter: Map<Char,List<Contact>> = groupBy { it.firstName.firstLetterOrDefault() }
		val result: MutableMap<Char, List<Contact>> = mutableMapOf()
		('A'..'Z').forEach { letter: Char ->
			result[letter] = contactsByFirstLetter[letter] ?: emptyList()
		}
		result['#'] = contactsByFirstLetter['#'] ?: emptyList()
		return result
	}

	private fun setupObservers() {
		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.filteredContactsFlow.collectLatest { res ->
					when (res.status) {
						Status.SUCCESS -> {
							val contacts: List<Contact> = res.data!!
							Log.d(TAG, "Caricati ${contacts.size} contatti")
							binding.swipeRefreshContacts.isRefreshing = false
							if (contacts.isEmpty()) {
								binding.contacts.swapAdapter(ContactsLoadingOrEmptyAdapter(ContactsLoadingOrEmptyAdapter.State.Empty), false)
							} else {
								contacts.groupedByFirstLetter().forEach { (letter, contacts) ->
									contactsAdapters[letter]?.updateContacts(contacts)
								}
								binding.contacts.swapAdapter(contactsAdapter, false)
							}
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
			}
		}

		viewModel.selectedFiltersCount.observe(viewLifecycleOwner) { count ->
			binding.contactsFilter.appliedFiltersCount.apply {
				if (count > 0) {
					isVisible = true
					text = "($count)"
				} else {
					isVisible = false
				}
			}
		}
	}

	companion object {
		private const val TAG = "MeetFragment"
	}

}
