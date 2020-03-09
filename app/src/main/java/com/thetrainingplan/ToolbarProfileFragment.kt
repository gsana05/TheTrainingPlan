package com.thetrainingplan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.thetrainingplan.databinding.FragmentToolbarProfileBinding
import com.thetrainingplan.viewmodels.ProfileViewModel

class ToolbarProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(activity!!).get(ProfileViewModel::class.java)

        val binding: FragmentToolbarProfileBinding = FragmentToolbarProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.finishProfileActivityEvent.observe(this, Observer {
            activity?.finish()
        })

        return binding.root
    }

}
