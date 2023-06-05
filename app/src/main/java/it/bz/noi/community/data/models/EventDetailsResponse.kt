// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class EventDetailsResponse(
    @SerializedName("EventDescription")
    val eventDescription: String,
    @SerializedName("StartDate")
    val startDate: String,
    @SerializedName("EndDate")
    val endDate: String
)