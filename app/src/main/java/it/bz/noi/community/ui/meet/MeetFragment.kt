package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.ui.SimpleListAdapter

class MeetFragment : Fragment() {

    private lateinit var items: List<String>
    private lateinit var meetAdapter: SimpleListAdapter

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
        meetAdapter = SimpleListAdapter(items)
        val root = inflater.inflate(R.layout.fragment_meet, container, false)
        root.findViewById<RecyclerView>(R.id.meetRecyclerView).adapter = meetAdapter
        return root
    }

}