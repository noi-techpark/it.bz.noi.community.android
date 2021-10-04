package it.bz.noi.community.ui.more

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.R

class AppVersionAdapter : RecyclerView.Adapter<AppVersionViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppVersionViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.view_holder_app_version, parent, false)
		return AppVersionViewHolder(view)
	}

	override fun onBindViewHolder(holder: AppVersionViewHolder, position: Int) {
		holder.bind()
	}

	override fun getItemCount() = 1
}

class AppVersionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	private val tvAppVersion: TextView = itemView.findViewById(R.id.tvAppVersion)

	fun bind() {
		tvAppVersion.text = "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
	}

}
