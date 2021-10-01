package it.bz.noi.community.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.ui.SimpleListAdapter
import it.bz.noi.community.ui.WebViewFragmentDirections

class MoreFragment: Fragment() {

	private val openLinkClickListener = View.OnClickListener {
		it?.let {
			val pos = recyclerView.getChildAdapterPosition(it)

			getUrlByItemPosition(pos)?.let { linkUrl ->
				val action = WebViewFragmentDirections.actionGlobalWebViewFragment()
				action.title = items[pos]
				action.url = linkUrl
				findNavController().navigate(action)
			}

		}
	}

    private lateinit var items: List<String>
    private lateinit var moreAdapter: SimpleListAdapter
	private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items = listOf(
            resources.getString(R.string.room_booking),
            resources.getString(R.string.more_item_onboarding),
            resources.getString(R.string.more_item_feedback))

		/*
		 * Items postponed to MVP2:
         *  resources.getString(R.string.more_item_account)
         *  resources.getString(R.string.more_item_settings)
		 */
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        moreAdapter = SimpleListAdapter(items, openLinkClickListener)

        val root = inflater.inflate(R.layout.fragment_more, container, false)
        recyclerView = root.findViewById(R.id.moreRecyclerView)
		recyclerView.adapter = moreAdapter

        return root
    }

	private fun getUrlByItemPosition(pos: Int): String? {
		return when (pos) {
			0 -> resources.getString(R.string.url_room_booking)
			1 -> null
			2 -> resources.getString(R.string.url_provide_feedback)
			else -> throw Exception("Link not found")
		}
	}

}
