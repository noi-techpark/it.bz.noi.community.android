package it.bz.noi.community.ui.more

import android.os.Handler
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.R
import android.view.MotionEvent

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

	private val onTouchListener = object : View.OnTouchListener {
		// Handler to handle the number of clicks
		var handler  = Handler()
		var numberOfTaps = 0
		var lastTapTimeMs: Long = 0
		var touchDownMs: Long = 0

		override fun onTouch(v: View?, event: MotionEvent): Boolean {
			when (event.action) {
				MotionEvent.ACTION_DOWN -> {
					touchDownMs = System.currentTimeMillis()
				}
				MotionEvent.ACTION_UP -> {
					// Handle the numberOfTaps
					handler.removeCallbacksAndMessages(null)

					if (System.currentTimeMillis() - touchDownMs
						> ViewConfiguration.getTapTimeout()
					) {
						//it was not a tap
						numberOfTaps = 0
						lastTapTimeMs = 0
					}
					if (numberOfTaps > 0
						&& System.currentTimeMillis() - lastTapTimeMs
						< ViewConfiguration.getDoubleTapTimeout()
					) {
						// if the view was clicked once
						numberOfTaps += 1
					} else {
						// if the view was never clicked
						numberOfTaps = 1
					}
					lastTapTimeMs = System.currentTimeMillis()

					if (numberOfTaps == 5) {
						throw Exception("Crash test")
					}
				}
			}
			return true
		}
	}

	init {
		itemView.isClickable = true
		// CRASHLYTICS TEST
		// We use five taps on the More tab footer to force a crash
		itemView.setOnTouchListener(onTouchListener)
	}

	fun bind() {
		tvAppVersion.text = "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
	}

}
