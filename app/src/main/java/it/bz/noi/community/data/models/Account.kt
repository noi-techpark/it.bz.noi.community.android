// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

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
	@SerializedName("crb14_accountcat_placepresscommunity")
	val type: String
)

data class AccountsResponse(
	@SerializedName("value")
	val accounts: List<Account>,
	@SerializedName("@odata.count")
	val count: Int?
)

enum class AccountType(val code: String? = null) {
	COMPANY("952210002"),
	STARTUP("952210001"),
	RESEARCH_INSTITUTION("952210003"),
	DEFAULT
}

fun Account.getAccountType(): AccountType {
	val categories = type.split(",")
	AccountType.values().forEach {
		if (categories.contains(it.code))
			return it
	}
	return AccountType.DEFAULT
}

fun Account.toFilterValue(): FilterValue {
	return FilterValue(key = id, type = type, desc = name)
}
