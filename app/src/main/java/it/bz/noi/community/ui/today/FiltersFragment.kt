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
            FiltersAdapter.Item.Header(resources.getString(R.string.filter_by_type)),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_type_public), false, FiltersAdapter.FilterType.EVENT_TYPE),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_type_noi), false, FiltersAdapter.FilterType.EVENT_TYPE),
            FiltersAdapter.Item.Header(resources.getString(R.string.filter_by_sector)),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_sector_green), false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_sector_food), false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_sector_digital), false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR),
            FiltersAdapter.Item.Filter(resources.getString(R.string.filter_sector_automotiv), false, FiltersAdapter.FilterType.TECHNOLOGY_SECTOR)
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