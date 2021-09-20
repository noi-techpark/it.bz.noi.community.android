package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.ui.SimpleListAdapter

class MeetFragment : Fragment() {

    private val openLinkClickListener = View.OnClickListener {
        it?.let {

            val pos = recyclerView.getChildAdapterPosition((it.parent as View))

            val action = MeetFragmentDirections.actionNavigationMeetToWebViewFragment()
            action.title = items[pos]
            action.url = meetViewModel.getUrlByItemPosition(pos)
            findNavController().navigate(action)
        }
    }

    private lateinit var items: List<String>
    private lateinit var meetAdapter: SimpleListAdapter
    private lateinit var recyclerView: RecyclerView
    private val meetViewModel = MeetViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items = listOf(
            resources.getString(R.string.meet_item_companies),
            resources.getString(R.string.meet_item_startups),
            resources.getString(R.string.meet_item_university),
            resources.getString(R.string.meet_item_research),
            resources.getString(R.string.meet_item_support),
            resources.getString(R.string.meet_item_lab),
            resources.getString(R.string.meet_item_team))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        meetAdapter = SimpleListAdapter(items, openLinkClickListener)

        val root = inflater.inflate(R.layout.fragment_meet, container, false)
        recyclerView = root.findViewById(R.id.meetRecyclerView)
        recyclerView.adapter = meetAdapter

        return root
    }

}