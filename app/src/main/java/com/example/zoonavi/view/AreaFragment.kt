package com.example.zoonavi.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoonavi.R
import com.example.zoonavi.databinding.AreaInfoItemLayoutBinding
import com.example.zoonavi.databinding.ListLayoutBinding
import com.example.zoonavi.databinding.PlantsListItemLayoutBinding
import com.example.zoonavi.model.Area
import com.example.zoonavi.model.Plant
import com.example.zoonavi.viewmodel.Status
import com.example.zoonavi.viewmodel.ZooViewModel
import kotlinx.coroutines.Job

class AreaFragment: Fragment() {
    private lateinit var areaName: String
    private var area: Area? = null
    private lateinit var viewBinding: ListLayoutBinding
    private val plantList: MutableList<Plant> = ArrayList()
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        areaName = arguments?.getString("name") ?: ""
        val viewModel: ZooViewModel by activityViewModels()
        viewBinding = ListLayoutBinding.inflate(inflater, container, false).apply {
            titleText.text = areaName
            backBtn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            listView.adapter = Adapter()
        }

        viewModel.areaForFragment.observe(viewLifecycleOwner) {
            if (it != null) {
                area = it
                viewBinding.listView.adapter?.notifyDataSetChanged()
            }
        }
        viewModel.plantsInArea.observe(viewLifecycleOwner) {
            plantList.clear()
            plantList.addAll(it)
            viewBinding.listView.adapter?.notifyDataSetChanged()
        }
        viewModel.areaFragmentLoadingStatus.observe(viewLifecycleOwner) {
            viewBinding.progressBar.visibility = if (it == Status.Loading) View.VISIBLE else View.GONE
        }
        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        val viewModel: ZooViewModel by activityViewModels()
        job = viewModel.setAreaFragmentInfo(areaName)
    }

    override fun onStop() {
        super.onStop()
        if (job?.isActive == true) {
            job?.cancel()
        }
    }

    private val itemCallback = object: ItemCallback {
        override fun onItemClick(plant: Plant) {
            val bundle = bundleOf("name" to plant.nameInEng)
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out)
                replace(R.id.container, PlantFragment::class.java, bundle)
                addToBackStack(null)
            }
        }
    }

    inner class Adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> AreaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.area_info_item_layout, parent, false))
                1 ->  {
                    object: RecyclerView.ViewHolder(TextView(parent.context).apply {
                        val viewHeight = 30*context.resources.displayMetrics.density
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight.toInt())
                        setPadding(context.resources.getDimension(R.dimen.content_padding).toInt(), 0, 0, 0)
                        gravity = Gravity.CENTER or Gravity.START
                        text = context.getString(R.string.plants)
                        textSize = 18f
                    }) {
                        //empty body
                    }
                }
                else -> PlantsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plants_list_item_layout, parent, false), itemCallback)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as AreaViewHolder).setData(area)
                1 -> {
                    //do nothing
                }
                else -> (holder as PlantsViewHolder).setData(plantList[position-2])
            }
        }

        override fun getItemCount(): Int = if (plantList.isEmpty()) 1 else plantList.size + 2
        override fun getItemViewType(position: Int): Int = position
    }

    class AreaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val viewBinding = AreaInfoItemLayoutBinding.bind(itemView)
        fun setData(area: Area?) {
            if (area != null) {
                Glide.with(itemView.context).load(area.picUrl).into(viewBinding.image)
                viewBinding.info.text = area.info
            }
        }
    }

    class PlantsViewHolder(itemView: View, val callback: ItemCallback): RecyclerView.ViewHolder(itemView) {
        private val viewBinding = PlantsListItemLayoutBinding.bind(itemView)
        fun setData(plant: Plant) {
            Glide.with(itemView.context).load(plant.mainPicUrl).placeholder(ColorDrawable(Color.GRAY)).into(viewBinding.image)
            viewBinding.title.text = plant.name
            viewBinding.brief.text = plant.alsoKnown
            itemView.setOnClickListener {
                callback.onItemClick(plant)
            }
        }
    }

    interface ItemCallback {
        fun onItemClick(plant: Plant)
    }
}