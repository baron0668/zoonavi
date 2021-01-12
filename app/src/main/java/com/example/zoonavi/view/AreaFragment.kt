package com.example.zoonavi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.zoonavi.databinding.ListLayoutBinding

class AreaFragment: Fragment() {
    private var areaName: String = ""
    private lateinit var viewBinding: ListLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        areaName = arguments?.getString("name")?: ""
        viewBinding = ListLayoutBinding.inflate(inflater, container, false)
        viewBinding.titleText.text = areaName
        viewBinding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return viewBinding.root
    }

}