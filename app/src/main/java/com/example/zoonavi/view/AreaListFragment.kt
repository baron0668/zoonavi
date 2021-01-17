package com.example.zoonavi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoonavi.R
import com.example.zoonavi.databinding.ListLayoutBinding
import com.example.zoonavi.model.Area
import com.example.zoonavi.viewmodel.ZooViewModel

class AreaListFragment: Fragment() {
    private lateinit var viewBinding: ListLayoutBinding
    private val areaList: MutableList<Area> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ListLayoutBinding.inflate(inflater, container, false)
        viewBinding.backBtn.visibility = View.GONE
        viewBinding.titleText.text = getString(R.string.zoo_name)
        viewBinding.listView.layoutManager = GridLayoutManager(context, 2)
        viewBinding.listView.adapter = Adapter()

        val viewModel: ZooViewModel by activityViewModels()
        viewModel.areas.observe(viewLifecycleOwner, Observer<List<Area>>(){
            areaList.clear()
            areaList.addAll(it)
            (viewBinding.listView.adapter as Adapter).notifyDataSetChanged()
        })
        viewModel.loadAreas()
        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()
        (viewBinding.listView.adapter as Adapter).notifyDataSetChanged()
    }

    private val itemCallback: ItemCallback = object: ItemCallback {
        override fun onItemClick(areaName: String) {
            val viewModel: ZooViewModel by activityViewModels()
            viewModel.resetPlantStatus()
            parentFragmentManager.commit {
                val bundle = bundleOf("name" to areaName)
                setReorderingAllowed(true)
                replace(R.id.container, AreaFragment::class.java, bundle)
                addToBackStack(null)
            }
        }
    }

    inner class Adapter: RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false), itemCallback)
        override fun getItemCount(): Int = areaList.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setData(areaList[position])
        }

    }

    class ViewHolder(inflatedView: View, val callback: ItemCallback): RecyclerView.ViewHolder(inflatedView) {
        private val imageView = (itemView as ViewGroup).findViewById<ImageView>(R.id.image)
        private val titleView= (itemView as ViewGroup).findViewById<TextView>(R.id.title)

        private var areaName: String? = null

        init {
            itemView.setOnClickListener {
                if (areaName != null) {
                    callback.onItemClick(areaName!!)
                }
            }
        }

        fun setData(area: Area) {
            Glide.with(itemView.context).load(area.picUrl).into(imageView)
            areaName = area.name
            titleView.text = area.name
        }
    }

    interface ItemCallback {
        fun onItemClick(areaName: String)
    }
}