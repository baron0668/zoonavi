package com.example.zoonavi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoonavi.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ZooViewModel(application: Application): AndroidViewModel(application) {
    val areas: MutableLiveData<List<Area>> = MutableLiveData()
    val plants: MutableLiveData<List<Plant>> = MutableLiveData()
    val areaRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val plantsRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    private val areaRepository = AreaRepository()
    private val plantRepository = PlantRepository(getApplication<Application>().applicationContext)

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

    fun getAreaByName(name: String): Area? {
        var result: Area? = null
        areas.value?.forEach {
            if (it.name == name) {
                result = it
                return@forEach
            }
        }
        return result
    }

    fun loadPlants(areaName: String) {
        if (plantsRepositoryStatus.value != Status.Loading) {
            viewModelScope.launch {
                plantsRepositoryStatus.postValue(Status.Loading)
                plantRepository.findPlants("%$areaName%").also {
                    plantsRepositoryStatus.postValue(Status.Done)
                    plants.postValue(it)
                }
            }
        }
    }
}

enum class Status {Init, Loading, Done, Error}