package it.bz.noi.community.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Contact(
	@SerializedName("contactid")
	val id: String,
	@SerializedName("firstname")
	val firstName: String,
	@SerializedName("lastname")
	val lastName: String,
	@SerializedName("fullname")
	val fullName: String,
	@SerializedName("emailaddress1")
	val email: String?,
	@SerializedName("_parentcustomerid_value")
	val accountId: String?,

	val companyName: String?
) : Parcelable

data class ContactResponse(
	@SerializedName("value")
	val contacts: List<Contact>,
	@SerializedName("@odata.count")
	val count: Int?
)
