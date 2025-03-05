// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.meet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Account
import it.bz.noi.community.data.models.AccountType
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.models.getAccountType
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentContactDetailsBinding
import it.bz.noi.community.utils.Utils.showDial
import it.bz.noi.community.utils.Utils.writeEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.TimeUnit

class ContactDetailsFragment : Fragment() {

	companion object {
		private const val TAG = "ContactDetailsFragment"
	}

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

			shareContactIcon.setOnClickListener {
				shareVCard(contact, company)
			}

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
		}

		(requireActivity() as AppCompatActivity).supportActionBar?.title = contact.fullName

	}

	private fun shareVCard(contact: Contact, company: Account?) {
		val vcfFile = createVCard(contact, company)

		vcfFile?.let { file ->
			// Crea un URI usando FileProvider per la compatibilità con Android 7+
			val fileUri = FileProvider.getUriForFile(
				requireContext(),
				"${requireContext().packageName}.fileprovider",
				file
			)

			// Intent per condividere il vCard
			val shareIntent = Intent(Intent.ACTION_SEND).apply {
				type = "text/x-vcard"
				putExtra(Intent.EXTRA_STREAM, fileUri)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}

			// Intent per salvare in rubrica
			val saveIntent = Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(fileUri, "text/x-vcard")
				flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
			}

			val chooserIntent = Intent.createChooser(shareIntent, "Condividi contatto")
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(saveIntent))
			startActivity(chooserIntent)
		}
	}

	private fun createVCard(contact: Contact, company: Account?): File? {
		return try {
			// Crea un file temporaneo per il vCard
			val vcfFile = File(requireContext().cacheDir, "${contact.fullName}.vcf")

			FileWriter(vcfFile).use { writer ->
				writer.write("BEGIN:VCARD\n")
				writer.write("VERSION:3.0\n")
				writer.write("N:${contact.lastName};${contact.firstName}\n")
				writer.write("FN:${contact.fullName}\n")

				company?.phoneNumber?.let {
					writer.write("TEL;TYPE=WORK:$it\n")
				}

				contact.email?.let {
					writer.write("EMAIL;TYPE=WORK:$it\n")
				}

				company?.let {
					writer.write("ORG:${company.name}\n")
				}

				writer.write("END:VCARD")
			}

			vcfFile
		} catch (e: IOException) {
			Log.e(TAG, "Error creating tmp file for contact vCard", e)
			null
		}
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
