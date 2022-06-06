package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.COMPANY_FILTER
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.RESEARCH_INSTITUTION_FILTER
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.STARTUP_FILTER

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

enum class AccountType(val code: String? = null, val filterCode: Int? = null) {
	COMPANY("952210003", COMPANY_FILTER),
	STARTUP("952210001", STARTUP_FILTER),
	RESEARCH_INSTITUTION("95221003", RESEARCH_INSTITUTION_FILTER),
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
