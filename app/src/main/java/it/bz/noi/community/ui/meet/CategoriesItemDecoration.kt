// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.meet

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import it.bz.noi.community.R

/**
 * Add spacing between items in the recyclerview.
 */
class CategoriesItemDecoration : ItemDecoration() {

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		val position = parent.getChildAdapterPosition(view)
		val margin = view.resources.getDimensionPixelSize(R.dimen.chip_spacing)
		when {
			position != 0 -> {
				outRect.left = margin
			}
			else -> Unit
		}
	}
}
