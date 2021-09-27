package it.bz.noi.community.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.ui.SimpleListAdapter

class MoreFragment: Fragment() {

    private lateinit var items: List<String>
    private lateinit var moreAdapter: SimpleListAdapter

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
        moreAdapter = SimpleListAdapter(items)
        val root = inflater.inflate(R.layout.fragment_more, container, false)
        root.findViewById<RecyclerView>(R.id.moreRecyclerView).adapter = moreAdapter
        return root
    }
}
