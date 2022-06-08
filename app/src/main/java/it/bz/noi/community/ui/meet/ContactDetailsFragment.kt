package it.bz.noi.community.ui.meet

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.bz.noi.community.R
import it.bz.noi.community.data.models.AccountType
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.models.getAccountType
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentContactDetailsBinding
import it.bz.noi.community.utils.Utils.findOnMaps
import it.bz.noi.community.utils.Utils.showDial
import it.bz.noi.community.utils.Utils.writeEmail
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

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

			company?.let { account ->
				getImageId(account.getAccountType())?.let { imageId ->
					contactImage.setImageDrawable(getDrawable(requireContext(), imageId))
				}
			}

			contactName.text = "${contact.firstName}\n${contact.lastName}"
			contactIcon.text = "${contact.firstName[0]}${contact.lastName[0]}"

			companyName.isVisible = contact.companyName != null
			companyName.text = contact.companyName

			if (contact.email != null) {
				email.apply {
					fieldLbl.text = getString(R.string.label_email)
					fieldValue.text = contact.email
					root.setOnClickListener {
						copyToClipboard("email_copied", contact.email)
						showCheckmark(email.copyValueIcon)
					}
				}

				binding.sendEmail.setOnClickListener {
					requireContext().writeEmail(receiverAddress = contact.email)
				}
			} else {
				email.root.isVisible = false
				binding.sendEmail.isVisible = false
			}

			if (company?.phoneNumber != null) {
				phone.apply {
					fieldLbl.text = getString(R.string.label_phone)
					fieldValue.text = company.phoneNumber
					root.setOnClickListener {
						copyToClipboard("phone_copied", company.phoneNumber)
						showCheckmark(phone.copyValueIcon)
					}
				}

				binding.call.setOnClickListener {
					requireContext().showDial(company.phoneNumber)
				}
			} else {
				phone.root.isVisible = false
				binding.call.isVisible = false
			}

			if (company?.address != null) {
				val formattedAddress = company.address.replace("\r\n", ", ")

				address.apply {
					fieldLbl.text = getString(R.string.label_address)
					fieldValue.text = formattedAddress
					root.setOnClickListener {
						copyToClipboard("address_copied", formattedAddress)
						showCheckmark(address.copyValueIcon)
					}
				}

				binding.find.setOnClickListener {
					requireContext().findOnMaps(formattedAddress)
				}
			} else {
				address.root.isVisible = false
				binding.find.isVisible = false
			}

		}

		(requireActivity() as AppCompatActivity).supportActionBar?.title = contact.fullName

	}

	private fun copyToClipboard(label: String, value: String) {
		val clipboard = getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager
		val clip: ClipData = ClipData.newPlainText(label, value)
		clipboard.setPrimaryClip(clip)
	}

	private fun showCheckmark(icon: ImageView) {
		icon.isSelected = true
		CoroutineScope(Dispatchers.IO).launch {
			delay(TimeUnit.SECONDS.toMillis(1))
			icon.isSelected = false
		}
	}

	private fun getImageId(accountType: AccountType): Int? {
		return when (accountType) {
			AccountType.COMPANY -> R.drawable.contact_detail_company
			AccountType.STARTUP -> R.drawable.contact_detail_startup
			AccountType.RESEARCH_INSTITUTION -> R.drawable.contact_detail_institution
			AccountType.DEFAULT -> null
		}
	}

}

class ContactDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

	companion object {
		private const val CONTACT_STATE = "contact"
	}

	val contact: Contact = savedStateHandle.get(CONTACT_STATE)
		?: throw IllegalStateException("Missing $CONTACT_STATE argument")

}
