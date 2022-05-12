package it.bz.noi.community.oauth

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class NOIJwtAccessToken(
	@SerialName("resource_access")
	val resourceAccess: Map<String, ResourceAccess>
)

@Serializable
data class ResourceAccess(
	@SerialName("roles")
	val roles: List<String> = emptyList()
)

fun NOIJwtAccessToken.checkResourceAccessRoles(resourceAccessName: String, rolesToCheck: List<String>): Boolean = resourceAccess[resourceAccessName]?.roles?.containsAll(rolesToCheck) ?: false

fun decode(idToken: String): NOIJwtAccessToken? {
	val segments = idToken.split(".")
	return when {
		segments.isEmpty() -> null
		else -> decodeJwtPayload(segments[1])
	}
}

private fun decodeJwtPayload(jwt: String): NOIJwtAccessToken = Json {
	isLenient = true
	ignoreUnknownKeys = true
}.decodeFromString(
	NOIJwtAccessToken.serializer(),
	String(Base64.decode(jwt, Base64.NO_PADDING and Base64.URL_SAFE), Charsets.UTF_8)
)
