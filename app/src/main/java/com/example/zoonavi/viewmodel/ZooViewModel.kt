package com.example.zoonavi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zoonavi.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.Reader

class ZooViewModel(application: Application): AndroidViewModel(application) {
    val areas: MutableLiveData<List<Area>> = MutableLiveData()
    val plantsInArea: MutableLiveData<List<Plant>> = MutableLiveData()
    var plantInInfo: Plant? = null
    val areaRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val plantsRepositoryStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val isPlantDbReady: MutableLiveData<Boolean> = MutableLiveData(false)
    private val repositoryCallback = object: Callback {
        override fun getAssetReader(fileName: String): Reader {
            return InputStreamReader(getApplication<Application>().applicationContext.resources.assets.open(fileName))
        }
    }
    private val areaRepository = AreaRepository(repositoryCallback)
    private val plantRepository = PlantRepository(getApplication<Application>().applicationContext, repositoryCallback)
    private var plantUpdateTask: Job? = null
    private var plantSearchTask: Job? = null

    fun loadAreas() {
        if (areaRepositoryStatus.value != Status.Loading) {
            viewModelScope.launch {
                areaRepositoryStatus.postValue(Status.Loading)
                areaRepository.getAreaList().also {
                    if (it == null) {
                        areaRepositoryStatus.postValue(Status.Error)
                    } else {
                        areaRepositoryStatus.postValue(Status.Done)
                        areas.postValue(it)
                    }
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
            plantsRepositoryStatus.postValue(Status.Loading)
            if (isPlantDbReady.value == false && plantUpdateTask?.isActive != true) {
                plantUpdateTask = viewModelScope.launch {
                    isPlantDbReady.postValue(plantRepository.updatePlantsDb())
                }
            }
            if (isPlantDbReady.value == true) {
                plantSearchTask = viewModelScope.launch {
                    plantRepository.findPlants("%$areaName%").also {
                        plantsRepositoryStatus.postValue(Status.Done)
                        plantsInArea.postValue(it)
                    }
                }
            } else {
                plantsRepositoryStatus.postValue(Status.Error)
            }
        }
    }

    fun resetPlantStatus() {
        plantSearchTask?.cancel()
        plantSearchTask = null
        plantsInArea.postValue(ArrayList())
        plantsRepositoryStatus.postValue(Status.Init)
    }
}

interface Callback {
    fun getAssetReader(fileName: String): Reader
}

enum class Status {Init, Loading, Done, Error}