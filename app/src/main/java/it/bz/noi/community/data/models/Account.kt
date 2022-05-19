package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class Account(
	@SerializedName("accountid")
	val id: String,
	@SerializedName("name")
	val name: String,
	@SerializedName("telephone1")
	val phoneNumber: String?,
	@SerializedName("address1_composite")
	val address: String?,
)

data class AccountsResponse(
	@SerializedName("value")
	val accounts: List<Account>,
	@SerializedName("@odata.count")
	val count: Int?
)
