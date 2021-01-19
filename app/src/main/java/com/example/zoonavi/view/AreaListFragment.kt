package com.example.zoonavi.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zoonavi.R
import com.example.zoonavi.databinding.ListLayoutBinding
import com.example.zoonavi.model.Area
import com.example.zoonavi.viewmodel.Status
import com.example.zoonavi.viewmodel.ZooViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job

class AreaListFragment: Fragment() {
    private lateinit var viewBinding: ListLayoutBinding
    private val areaList: MutableList<Area> = ArrayList()
    private var job: Job? = null
    private lateinit var locationClient: FusedLocationProviderClient
    private val permissionRequestCode = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ListLayoutBinding.inflate(inflater, container, false).apply {
            backBtn.visibility = View.GONE
            rightBtn.setImageResource(R.drawable.ic_baseline_add_location_24)
            rightBtn.visibility = View.VISIBLE
            rightBtn.setOnClickListener(onLocationClick)
            titleText.text = getString(R.string.zoo_name)
            listView.layoutManager = GridLayoutManager(context, 2)
            listView.adapter = Adapter()
        }

        val viewModel: ZooViewModel by activityViewModels()
        viewModel.areas.observe(viewLifecycleOwner) {
            areaList.clear()
            areaList.addAll(it)
            areaList.sortBy {
                it.distance
            }
            (viewBinding.listView.adapter as Adapter).notifyDataSetChanged()
        }
        viewModel.areaListFragmentLoadingStatus.observe(viewLifecycleOwner) {
            viewBinding.progressBar.visibility = if (it == Status.Loading) View.VISIBLE else View.GONE
        }
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        val viewModel: ZooViewModel by activityViewModels()
        job = viewModel.setAreaListFragmentInfo()
    }

    override fun onStop() {
        super.onStop()
        if (job?.isActive == true) {
            job?.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        (viewBinding.listView.adapter as Adapter).notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationInfo()
            } else {
                Log.d("test","permission not got")
            }
        }
    }

    private val onLocationClick = View.OnClickListener {
        when {
            ContextCompat.checkSelfPermission(
                    it.context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationInfo()
            }
            else -> {
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        permissionRequestCode)
            }
        }

    }

    private val itemCallback: ItemCallback = object: ItemCallback {
        override fun onItemClick(areaName: String) {
            val viewModel: ZooViewModel by activityViewModels()
            viewModel.resetPlantStatus()
            parentFragmentManager.commit {
                val bundle = bundleOf("name" to areaName)
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out)
                replace(R.id.container, AreaFragment::class.java, bundle)
                addToBackStack(null)
            }
        }
    }

    private fun getLocationInfo() {
        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val viewModel: ZooViewModel by activityViewModels()
                                    viewModel.setLocationGeoInfo(location)
                                }
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
        private val distanceTextView = (itemView as ViewGroup).findViewById<TextView>(R.id.distance)
        private val titleView = (itemView as ViewGroup).findViewById<TextView>(R.id.title)

        private var areaName: String? = null

        init {
            itemView.setOnClickListener {
                if (areaName != null) {
                    callback.onItemClick(areaName!!)
                }
            }
        }

        fun setData(area: Area) {
            Glide.with(itemView.context).load(area.picUrl).placeholder(ColorDrawable(Color.GRAY)).into(imageView)
            areaName = area.name
            titleView.text = area.name
            distanceTextView.text = "${area.distance} m"
            distanceTextView.visibility = if (area.distance != -1) View.VISIBLE else View.INVISIBLE
        }
    }

    interface ItemCallback {
        fun onItemClick(areaName: String)
    }
}