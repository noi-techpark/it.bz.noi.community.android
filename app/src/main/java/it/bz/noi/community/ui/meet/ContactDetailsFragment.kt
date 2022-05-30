package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentContactDetailsBinding

class ContactDetailsFragment : Fragment() {

	private var _binding: FragmentContactDetailsBinding? = null
	private val binding get() = _binding!!

	private val viewModel: ContactDetailsViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val contact = viewModel.contact
		val company = AccountsManager.availableCompanies.value[contact.accountId]
		binding.apply {
			contactName.text = "${contact.firstName}\n${contact.lastName}"
			contactIcon.text = "${contact.firstName[0]}${contact.lastName[0]}"

			companyName.isVisible = contact.companyName != null
			companyName.text = contact.companyName

			if (contact.email != null) {
				email.apply {
					fieldLbl.text = getString(R.string.label_email)
					fieldValue.text = contact.email
					root.setOnClickListener {
						Toast.makeText(requireContext(), "Copy email", Toast.LENGTH_SHORT).show()
						// TODO
					}
				}
			} else {
				email.root.isVisible = false
			}

			if (company?.phoneNumber != null) {
				phone.apply {
					fieldLbl.text = getString(R.string.label_phone)
					fieldValue.text = company.phoneNumber
					root.setOnClickListener {
						Toast.makeText(requireContext(), "Copy telephone", Toast.LENGTH_SHORT)
							.show()
						// TODO
					}
				}
			} else {
				phone.root.isVisible = false
			}

			if (company?.address != null) {
				address.apply {
					fieldLbl.text = getString(R.string.label_address)
					fieldValue.text = company.address.replace("\r\n", " ")
					root.setOnClickListener {
						Toast.makeText(requireContext(), "Copy address", Toast.LENGTH_SHORT).show()
						// TODO
					}
				}
			} else {
				address.root.isVisible = false
			}

		}

		(requireActivity() as AppCompatActivity).supportActionBar?.title = contact.fullName

	}

}

class ContactDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

	companion object {
		private const val CONTACT_STATE = "contact"
	}

	val contact: Contact = savedStateHandle.get(CONTACT_STATE)
		?: throw IllegalStateException("Missing ${CONTACT_STATE} argument")

}
