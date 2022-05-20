package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.oauth.AuthManager.application
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.SimpleListAdapter
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.WebViewFragmentDirections

class MeetFragmentOLD : Fragment() {

	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), JsonFilterRepository(application))
	})

    private val openLinkClickListener = View.OnClickListener {
        it?.let {
            val pos = recyclerView.getChildAdapterPosition(it)

            val action = WebViewFragmentDirections.actionGlobalWebViewFragment().apply {
				title = items[pos]
				url = getUrlByItemPosition(pos)
			}
            findNavController().navigate(action)
        }
    }

    private lateinit var items: List<String>
    private lateinit var meetAdapter: SimpleListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items = listOf(
            resources.getString(R.string.meet_item_companies),
            resources.getString(R.string.meet_item_startups),
            resources.getString(R.string.meet_item_university),
            resources.getString(R.string.meet_item_research),
            resources.getString(R.string.meet_item_institutions),
            resources.getString(R.string.meet_item_lab),
            resources.getString(R.string.meet_item_team))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        meetAdapter = SimpleListAdapter(items, openLinkClickListener)

        val root = inflater.inflate(R.layout.fragment_meet_old, container, false)
        recyclerView = root.findViewById(R.id.meetRecyclerView)
        recyclerView.adapter = meetAdapter

        return root
    }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// FIXME
		mainViewModel.availableContacts.observe(viewLifecycleOwner) {
			if (it.isNotEmpty()) {
				Log.d(TAG, "Elenco contatti caricato")
			}
		}
	}

    private fun getUrlByItemPosition(pos: Int): String {
        return when (pos) {
            0 -> resources.getString(R.string.url_companies)
            1 -> resources.getString(R.string.url_startups)
            2 -> resources.getString(R.string.url_university)
            3 -> resources.getString(R.string.url_research)
            4 -> resources.getString(R.string.url_institutions)
            5 -> resources.getString(R.string.url_lab)
            6 -> resources.getString(R.string.url_about_us)
            else -> throw Exception("Link not found")
        }
    }

	companion object {
		private const val TAG = "MeetFragment"
	}

}
