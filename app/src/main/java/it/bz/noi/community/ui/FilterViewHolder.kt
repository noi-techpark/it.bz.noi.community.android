package it.bz.noi.community.ui

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import it.bz.noi.community.R
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding

class HeaderViewHolder(private val binding: VhHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(header: String) {
		binding.headerTextView.text = header
	}

}

class FilterViewHolder(private val binding: VhSwitchBinding, updateResultsListener: UpdateResultsListener, exclusive: Boolean = false) : RecyclerView.ViewHolder(binding.root) {

	private lateinit var filter: FilterValue

	init {
		binding.switchVH.setOnClickListener {
			filter.checked = binding.switchVH.isChecked

			if (exclusive && filter.checked)
				turnOffOtherSwitch()

			updateResultsListener.updateResults()
		}
	}

	private fun turnOffOtherSwitch() {
		val parent = binding.root.parent
		if (parent != null && parent is RecyclerView) {
			parent.apply {
				for (i in 1 until 3) {
					val childView = getChildAt(i)
					childView.findViewById<SwitchMaterial>(R.id.switchVH)?.let { switch ->
						if (!switch.text.equals(filter.desc)) {
							switch.isChecked = false
							switch.callOnClick()
						}
					}

				}
			}
		}
	}

	fun bind(f: FilterValue) {
		filter = f
		binding.switchVH.text = filter.desc
		binding.switchVH.isChecked = filter.checked
	}

}

interface UpdateResultsListener {
	fun updateResults()
}
