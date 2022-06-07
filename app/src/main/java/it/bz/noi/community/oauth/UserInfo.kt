package it.bz.noi.community.oauth

import com.google.gson.annotations.SerializedName

data class UserInfo(

	@SerializedName("sub")
	val subjectId: String,
	@SerializedName("name")
	val fullname: String,
	@SerializedName("given_name")
	val firstName: String,
	@SerializedName("family_name")
	val lastName: String,
	@SerializedName("email")
	val email: String

)
