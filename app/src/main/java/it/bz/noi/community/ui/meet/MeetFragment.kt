package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import it.bz.noi.community.R

class MeetFragment : Fragment() {

    private lateinit var meetViewModel: MeetViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        meetViewModel =
                ViewModelProvider(this).get(MeetViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_meet, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        meetViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}