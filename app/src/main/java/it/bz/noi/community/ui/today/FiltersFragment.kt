package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.ui.SimpleListAdapter

class FiltersFragment : Fragment() {

    private lateinit var items: List<FiltersAdapter.Item>
    private lateinit var filterAdapter: FiltersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items = listOf(
            FiltersAdapter.Item.Header("Filter by event type"),
            FiltersAdapter.Item.Filter("Public", false, FiltersAdapter.FilterType.EVENT_TYPE),
            FiltersAdapter.Item.Filter("NOI only", false, FiltersAdapter.FilterType.EVENT_TYPE),
            FiltersAdapter.Item.Header("Filter by technology sector"),
            FiltersAdapter.Item.Filter("Green", true, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter("Food", false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter("Digital", false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter("Automotiv", false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        filterAdapter = FiltersAdapter(items)
        val root = inflater.inflate(R.layout.fragment_filters, container, false)
        root.findViewById<RecyclerView>(R.id.filterstRV).adapter = filterAdapter
        return root
    }

}