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
import com.example.zoonavi.viewmodel.Status
import com.example.zoonavi.viewmodel.ZooViewModel
import kotlinx.coroutines.Job

class PlantFragment: Fragment() {
    private lateinit var plantNameInEng: String
    private var plant: Plant? = null
    private lateinit var viewBinding: PlantInfoLayoutBinding
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        plantNameInEng = arguments?.getString("name") ?: ""
        val viewModel by activityViewModels<ZooViewModel>()
        viewModel.plantForFragment.observe(viewLifecycleOwner) {
            if (it != null) {
                plant = it
                viewBinding.apply {
                    titleText.text = plant?.name
                    info.text = StringBuilder()
                            .appendLine(plant?.nameInEng).appendLine()
                            .appendLine(getString(R.string.also_know))
                            .appendLine(plant?.alsoKnown).appendLine()
                            .appendLine(getString(R.string.brief))
                            .appendLine(plant?.briefInfo).appendLine()
                            .appendLine(getString(R.string.feature))
                            .appendLine(plant?.featureInfo).appendLine()
                            .appendLine(getString(R.string.application)).appendLine()
                            .appendLine(plant?.applicationInfo).appendLine()
                            .append(getString(R.string.updated)).append(": ${plant?.updateDate}")
                            .toString()
                    Glide.with(requireContext()).load(plant?.mainPicUrl).into(image)
                }
            }
        }
        viewModel.plantFragmentLoadingStatus.observe(viewLifecycleOwner) {
            viewBinding.progressBar.visibility = if (it == Status.Loading) View.VISIBLE else View.GONE
        }

        viewBinding = PlantInfoLayoutBinding.inflate(inflater, container, false).apply {
            backBtn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
            info.movementMethod = ScrollingMovementMethod()
        }
        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        val viewModel by activityViewModels<ZooViewModel>()
        job = viewModel.setPlantFragmentInfo(plantNameInEng)
    }

    override fun onStop() {
        super.onStop()
        if (job?.isActive == true) {
            job?.cancel()
        }
    }
}