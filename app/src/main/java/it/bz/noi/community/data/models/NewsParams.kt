package it.bz.noi.community.data.models

data class NewsParams(
	var startDate: String,
	var pageSize: Int = 10,
	var pageNumber: Int,
	var language: String?
)
