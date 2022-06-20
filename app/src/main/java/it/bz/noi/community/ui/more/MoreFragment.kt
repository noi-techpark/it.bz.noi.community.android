package it.bz.noi.community.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentMoreBinding
import it.bz.noi.community.ui.SimpleListAdapter
import it.bz.noi.community.ui.WebViewFragmentDirections
import it.bz.noi.community.utils.Utils.openLinkInExternalBrowser

class MoreFragment : Fragment() {

	private lateinit var binding: FragmentMoreBinding

	private val openLinkClickListener = View.OnClickListener {
		it?.let {

			when (val pos = binding.moreRecyclerView.getChildAdapterPosition(it)) {
				PROFILE_ITEM -> findNavController().navigate(MoreFragmentDirections.actionMoreToProfile())
				APP_UPDATE_ITEM -> {
					requireContext().openLinkInExternalBrowser(getUrlByItemPosition(pos))
				}
				else -> {
					val action = WebViewFragmentDirections.actionGlobalWebViewFragment().apply {
						title = items[pos]
						url = getUrlByItemPosition(pos)
					}
					findNavController().navigate(action)
				}
			}
		}
	}

	private lateinit var items: List<String>
	private lateinit var moreInfoAdapter: SimpleListAdapter
	private val appVersionAdapter = AppVersionAdapter()
	private val moreAdapter: ConcatAdapter by lazy {
		ConcatAdapter(moreInfoAdapter, appVersionAdapter)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		items = listOf(
			resources.getString(R.string.room_booking),
			resources.getString(R.string.more_item_onboarding),
			resources.getString(R.string.more_item_feedback),
			resources.getString(R.string.more_item_bug_report),
			resources.getString(R.string.more_item_app_update),
			resources.getString(R.string.more_item_account),
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentMoreBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		moreInfoAdapter = SimpleListAdapter(items, openLinkClickListener)
		binding.moreRecyclerView.adapter = moreAdapter
	}

	private fun getUrlByItemPosition(pos: Int): String {
		return when (pos) {
			BOOK_A_ROOM_ITEM -> resources.getString(R.string.url_room_booking)
			COME_ON_BOARD_ITEM -> resources.getString(R.string.url_onboarding)
			PROVIDE_FEEDBACK_ITEM -> resources.getString(R.string.url_provide_feedback)
			BUG_REPORT_ITEM -> resources.getString(R.string.url_bug_report)
			APP_UPDATE_ITEM -> resources.getString(R.string.url_google_playstore)
			else -> throw Exception("Link not found")
		}
	}

	companion object {
		private const val BOOK_A_ROOM_ITEM = 0
		private const val COME_ON_BOARD_ITEM = 1
		private const val PROVIDE_FEEDBACK_ITEM = 2
		private const val BUG_REPORT_ITEM = 3
		private const val APP_UPDATE_ITEM = 4
		private const val PROFILE_ITEM = 5
	}

}
