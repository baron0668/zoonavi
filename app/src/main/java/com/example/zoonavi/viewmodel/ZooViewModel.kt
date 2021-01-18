package com.example.zoonavi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zoonavi.model.Area
import com.example.zoonavi.model.AreaRepository
import com.example.zoonavi.model.Plant
import com.example.zoonavi.model.PlantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.Reader

class ZooViewModel(application: Application): AndroidViewModel(application) {
    val areas: MutableLiveData<List<Area>> = MutableLiveData()
    val plantsInArea: MutableLiveData<List<Plant>> = MutableLiveData()
    val areaForFragment: MutableLiveData<Area> = MutableLiveData()
    val plantForFragment: MutableLiveData<Plant> = MutableLiveData()
    val areaListFragmentLoadingStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val areaFragmentLoadingStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    val plantFragmentLoadingStatus: MutableLiveData<Status> = MutableLiveData(Status.Init)
    private val repositoryCallback = object: Callback {
        override fun getAssetReader(fileName: String): Reader {
            return InputStreamReader(getApplication<Application>().applicationContext.resources.assets.open(fileName))
        }
    }
    private val areaRepository = AreaRepository(repositoryCallback)
    private val plantRepository = PlantRepository(getApplication<Application>().applicationContext, repositoryCallback)

    fun setAreaListFragmentInfo(): Job {
        return viewModelScope.launch {
            if (areaListFragmentLoadingStatus.value != Status.Loading) {
                areaListFragmentLoadingStatus.postValue(Status.Loading)
                loadAreas()
                areaListFragmentLoadingStatus.postValue(Status.Done)
            }
        }
    }

    fun setAreaFragmentInfo(areaName: String): Job {
        return viewModelScope.launch {
            if (areaFragmentLoadingStatus.value != Status.Loading) {
                areaFragmentLoadingStatus.postValue(Status.Loading)
                loadAreas()
                //find area
                areas.value?.forEach {
                    if (it.name == areaName) {
                        areaForFragment.postValue(it)
                    }
                }
                plantRepository.updatePlantsDb()
                plantRepository.findPlantsByArea("%$areaName%").also {
                    plantsInArea.postValue(it)
                }
                areaFragmentLoadingStatus.postValue(Status.Done)
            }
        }
    }

    fun setPlantFragmentInfo(plantNameInEn: String): Job {
        return viewModelScope.launch {
            if (plantFragmentLoadingStatus.value != Status.Loading) {
                plantFragmentLoadingStatus.postValue(Status.Loading)
                plantRepository.updatePlantsDb()
                plantForFragment.postValue(plantRepository.findPlantByName(plantNameInEn))
                plantFragmentLoadingStatus.postValue(Status.Done)
            }
        }
    }

    private suspend fun loadAreas() {
        areaRepository.getAreaList().also {
            areas.postValue(it)
        }
    }

    fun resetPlantStatus() {
        plantsInArea.postValue(ArrayList())
        plantForFragment.postValue(null)
        plantFragmentLoadingStatus.postValue(Status.Init)
    }
}

interface Callback {
    fun getAssetReader(fileName: String): Reader
}

enum class Status {Init, Loading, Done}