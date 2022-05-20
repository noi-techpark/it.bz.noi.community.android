package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

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
)

data class ContactResponse(
	@SerializedName("value")
	val contacts: List<Contact>,
	@SerializedName("@odata.count")
	val count: Int?
)
