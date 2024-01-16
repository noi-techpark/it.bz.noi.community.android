// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.utils

import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import androidx.annotation.ColorInt

fun SpannableStringBuilder.addClickableSpan(substring: String, onClick: () -> Unit) {
	val start = indexOf(substring).takeIf {
		it >= 0
	} ?: return
	val end = start + substring.length
	setSpan(
		object : ClickableSpan() {
			override fun onClick(widget: View) {
				onClick()
			}
		},
		start,
		end,
		SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
	)
}

fun SpannableStringBuilder.addLinkSpan(substring: String, @ColorInt color: Int) {
	val start = indexOf(substring).takeIf {
		it >= 0
	} ?: return
	val end = start + substring.length
	setSpan(
		ForegroundColorSpan(color),
		start,
		end,
		SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
	)
}

fun SpannableStringBuilder.addLinkSpan(substring: String, url: String) {
	val start = indexOf(substring).takeIf {
		it >= 0
	} ?: return
	val end = start + substring.length
	setSpan(
		URLSpan(url),
		start,
		end,
		SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
	)
}
