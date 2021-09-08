package it.bz.noi.community.ui.orientate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.bz.noi.community.R

class OrientateFragment : Fragment() {

    private lateinit var orientateViewModel: OrientateViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        orientateViewModel =
                ViewModelProvider(this).get(OrientateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_orientate, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        orientateViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}