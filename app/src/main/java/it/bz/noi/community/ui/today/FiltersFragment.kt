package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.resetFilters
import it.bz.noi.community.databinding.FragmentFiltersBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status

class FiltersFragment : Fragment() {

    private lateinit var items: List<FiltersAdapter.Item>
    private lateinit var filterAdapter: FiltersAdapter
    private lateinit var binding: FragmentFiltersBinding

    private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
        ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
    })

    private val onSwitchClickListener = View.OnClickListener{
        it?.let {
            val switch = it as SwitchMaterial
            val filter = switch.text
            val checked = switch.isChecked

            when (filter) {
                getString(R.string.filter_type_public) -> mainViewModel.urlParams.public = checked
                getString(R.string.filter_type_noi) -> mainViewModel.urlParams.noiOnly = checked
                getString(R.string.filter_sector_green) -> mainViewModel.urlParams.green = checked
                getString(R.string.filter_sector_food) -> mainViewModel.urlParams.food = checked
                getString(R.string.filter_sector_digital) -> mainViewModel.urlParams.digital = checked
                getString(R.string.filter_sector_automotiv) -> mainViewModel.urlParams.automotiv = checked
                else -> throw (RuntimeException("Filter not matched!"))
            }
            mainViewModel.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items = listOf(
            FiltersAdapter.Item.Header(resources.getString(R.string.filter_by_type)),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_type_public),
                mainViewModel.urlParams.public,
                FiltersAdapter.FilterType.EVENT_TYPE
            ),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_type_noi),
                mainViewModel.urlParams.noiOnly,
                FiltersAdapter.FilterType.EVENT_TYPE
            ),
            FiltersAdapter.Item.Header(resources.getString(R.string.filter_by_sector)),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_sector_green),
                mainViewModel.urlParams.green,
                FiltersAdapter.FilterType.TECHNOLOGY_SECTOR
            ),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_sector_food),
                mainViewModel.urlParams.food,
                FiltersAdapter.FilterType.TECHNOLOGY_SECTOR
            ),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_sector_digital),
                mainViewModel.urlParams.digital,
                FiltersAdapter.FilterType.TECHNOLOGY_SECTOR
            ),
            FiltersAdapter.Item.Filter(
                resources.getString(R.string.filter_sector_automotiv),
                mainViewModel.urlParams.automotiv,
                FiltersAdapter.FilterType.TECHNOLOGY_SECTOR
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFiltersBinding.inflate(inflater)
        mainViewModel.mediatorEvents.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    // TODO mostrare loader sul pulsante
                }
                Status.SUCCESS -> {
                    updateNumberOfResults(it.data?.size ?: 0)
                }
                Status.ERROR -> {
                    // TODO
                }
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterAdapter = FiltersAdapter(items, onSwitchClickListener)
        binding.apply {
            filterstRV.adapter = filterAdapter

            resetBtn.setOnClickListener {
                resetFilters()
            }

            showBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    private fun updateNumberOfResults(numResults: Int?) {
        binding.showBtn.text = getString(R.string.show_btn, numResults ?: 0)
    }

    private fun resetFilters() {
        items.iterator().forEach { item ->
            if (item is FiltersAdapter.Item.Filter) {
                item.checked = false
            }
        }
        filterAdapter.notifyDataSetChanged()
        mainViewModel.urlParams.resetFilters()
        mainViewModel.refresh()
    }

}
