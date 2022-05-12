package it.bz.noi.community.oauth

import com.google.gson.annotations.SerializedName

data class UserInfo(

	@SerializedName("sub")
	val subjectId: String,
	@SerializedName("name")
	val fullname: String,
	@SerializedName("email")
	val email: String

)

/*
nameValuePairs = {LinkedHashMap@19672}  size = 7
 "sub" -> "2e07dd7a-3d4c-4abd-b92c-24bc55dd0c8e"
 "email_verified" -> {Boolean@19686} true
 "name" -> "Matteo Matassoni"
 "preferred_username" -> "dev@dimension.it"
 "given_name" -> "Matteo"
 "family_name" -> "Matassoni"
 "email" -> "dev@dimension.it"
 */
