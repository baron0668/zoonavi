package com.example.zoonavi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoonavi.model.Area
import com.example.zoonavi.model.AreaRepository
import com.example.zoonavi.model.Plant
import com.example.zoonavi.model.PlantRespoitory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ZooViewModel: ViewModel() {
    val areas: MutableLiveData<List<Area>> = MutableLiveData()
    val plants: MutableLiveData<List<Plant>> = MutableLiveData()
    val areaRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val plantsRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    private val areaRepository = AreaRepository()
    private val plantRepository = PlantRespoitory()

    fun loadAreas() {
        if (areaRepositoryStatus.value != Status.Loading) {
            viewModelScope.launch {
                areaRepositoryStatus.postValue(Status.Loading)
                areaRepository.getAreaList().also {
                    areaRepositoryStatus.postValue(Status.Done)
                    areas.postValue(it)
                }
            }
        }
    }

    private fun loadPlants() {

    }
}

enum class Status {Init, Loading, Done, Error}