package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.*
import retrofit2.http.*

interface CommunityApiService {
    @GET("/accounts?@p1='crb14_accountcat_placepresscommunity'&@p2=['952210000']&\$select=name,noi_nameit,telephone1,address1_composite,crb14_accountcat_placepresscommunity&\$filter=Microsoft.Dynamics.CRM.ContainValues(PropertyName%3D%40p1,PropertyValues%3D%40p2)%20and%20statuscode%20eq%201&\$count=true")
    suspend fun getAccounts(
		@Header("Authorization") accessToken: String
	): AccountsResponse

	@GET("contacts?@p1='noi_contactcat_placepresscommunity'&@p2=['181640000']&\$select=emailaddress1,firstname,lastname,fullname,_parentcustomerid_value&\$filter=Microsoft.Dynamics.CRM.ContainValues(PropertyName%3D@p1,PropertyValues%3D@p2)%20and%20statuscode%20eq%201&\$count=true&\$orderby=fullname")
	suspend fun getContacts(
		@Header("Authorization") accessToken: String
	): ContactResponse
}
