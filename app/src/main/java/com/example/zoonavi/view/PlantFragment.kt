package com.example.zoonavi.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.zoonavi.R
import com.example.zoonavi.databinding.PlantInfoLayoutBinding
import com.example.zoonavi.model.Plant
import com.example.zoonavi.viewmodel.ZooViewModel

class PlantFragment: Fragment() {
    private lateinit var plant: Plant
    private lateinit var viewBinding: PlantInfoLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel by activityViewModels<ZooViewModel>()
        plant = viewModel.plantInInfo!!
        viewBinding = PlantInfoLayoutBinding.inflate(inflater, container, false).apply {
            titleText.text = plant.nameInEng
            backBtn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
            Glide.with(requireContext()).load(plant.mainPicUrl).into(image)
            info.movementMethod = ScrollingMovementMethod()
            info.text = StringBuilder()
                    .appendLine(plant.nameInEng).appendLine()
                    .appendLine(getString(R.string.also_know))
                    .appendLine(plant.alsoKnown).appendLine()
                    .appendLine(getString(R.string.brief))
                    .appendLine(plant.briefInfo).appendLine()
                    .appendLine(getString(R.string.feature))
                    .appendLine(plant.featureInfo).appendLine()
                    .appendLine(getString(R.string.application)).appendLine()
                    .appendLine(plant.applicationInfo).appendLine()
                    .append(getString(R.string.updated)).append(": ${plant.updateDate}")
                    .toString()
        }
        return viewBinding.root
    }


}