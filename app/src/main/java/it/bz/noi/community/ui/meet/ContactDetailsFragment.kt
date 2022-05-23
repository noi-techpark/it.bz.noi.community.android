package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
				email.text = contact.email
			} else {
				emailLbl.isVisible = false
				email.isVisible = false
				emailBtn.isVisible = false
				sendEmail.isVisible = false
			}

			if (company?.phoneNumber != null) {
				phone.text = company.phoneNumber
			} else {
				phoneLbl.isVisible = false
				phone.isVisible = false
				phoneBtn.isVisible = false
				call.isVisible = false
			}

			if (company?.address != null) {
				address.text = company.address.replace("\r\n", " ")
			} else {
				addressLbl.isVisible = false
				address.isVisible = false
				addressBtn.isVisible = false
				find.isVisible = false
			}

		}

		(requireActivity() as AppCompatActivity).supportActionBar?.title = contact.fullName

	}

}

class ContactDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

	companion object {
		private const val CONTACT_STATE = "contact"
	}

	val contact: Contact = savedStateHandle.get(CONTACT_STATE) ?: throw IllegalStateException("Missing ${CONTACT_STATE} argument")

}
