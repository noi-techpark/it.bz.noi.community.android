package it.bz.noi.community.data.models

data class FilterValue(
	val key: String,
	val type: String,
	val desc: String,
	var checked: Boolean = false
)
