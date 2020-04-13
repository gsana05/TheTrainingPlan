package com.thetrainingplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.FragmentToolbarBinding
import com.thetrainingplan.viewmodels.MainViewModel


class ToolbarFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toolbar, container, false)*/

        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)

        val binding: FragmentToolbarBinding = FragmentToolbarBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        /*viewModel.startProfileActivityEvent.observe(this, Observer {
            val intent = Intent(activity, ActivityProfile::class.java)
            startActivity(intent)
        })*/

        return binding.root
    }

}
