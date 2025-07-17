// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.common

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pad the bottom of the view to avoid system bars overlapping the content.
 * This is typically used for footers.
 */
fun View.handleEdgeToEdgeAsFooter() = ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
	val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
	v.updatePadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
	WindowInsetsCompat.CONSUMED
}

/**
 * Pad the top of the view to avoid system bars overlapping the content.
 * This is typically used for toolbars.
 */
fun View.handleEdgeToEdgeAsToolbar() = ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
	val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
	v.updatePadding(systemBars.left, systemBars.top, systemBars.right, 0)
	WindowInsetsCompat.CONSUMED
}

/**
 * Pad the view to avoid system bars overlapping the content.
 * This is typically used for full-screen containers, like RecyclerViews or Fragments.
 */
fun View.handleEdgeToEdgeInsetsByPadding(block: (Insets) -> Insets) = ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
	val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
	val insets = block(systemBars)
	v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
	WindowInsetsCompat.CONSUMED
}
