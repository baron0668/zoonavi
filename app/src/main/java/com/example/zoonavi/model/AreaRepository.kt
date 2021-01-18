package com.example.zoonavi.model

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zoonavi.viewmodel.Callback
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class AreaRepository(val callback: Callback) {
    private val api: Api = Api()
    private var dataList: MutableList<Area>? = null

    suspend fun getAreaList(): List<Area>? {
        return withContext(Dispatchers.IO) {
            if (dataList == null) {
                val apiResult = api.sendRequest(Api.Source.Area)
                if (apiResult != null) {
                    dataList = ArrayList(parseJson(apiResult))
                } else {
                    dataList = ArrayList(readFromOfflineData())
                }
            }
            dataList
        }
    }

    private suspend fun readFromOfflineData(): List<Area> {
        val resultList: MutableList<Area> = ArrayList()
        val csvReader = CSVReader(callback.getAssetReader("areas.csv"))
        val fieldNameArray = csvReader.readNext()
        val fieldIndexArray = Array<Int>(6) {
            fieldNameArray.indexOf(when(it) {
                0 -> "E_no"
                1 -> "E_Name"
                2 -> "E_Category"
                3 -> "E_Info"
                4 -> "E_Pic_URL"
                else -> "E_URL"
            })
        }
        csvReader.readAll().forEach {
            resultList.add(Area(
                it[fieldIndexArray[0]].toInt(),
                it[fieldIndexArray[1]],
                it[fieldIndexArray[2]],
                it[fieldIndexArray[3]],
                it[fieldIndexArray[4]],
                it[fieldIndexArray[5]]
            ))
        }
        csvReader.close()
        return resultList
    }

    private fun parseJson(jsonString: String): List<Area> {
        val resultList: MutableList<Area> = ArrayList()
        JSONObject(jsonString)
                .optJSONObject("result")
                ?.optJSONArray("results")?.also {resultsArray ->
                    for (i in 0 until resultsArray.length()) {
                        resultsArray.getJSONObject(i).also {
                            try {
                                resultList.add(Area(
                                        it.getInt("_id"),
                                        it.getString("E_Name"),
                                        it.getString("E_Category"),
                                        it.getString("E_Info"),
                                        it.getString("E_Pic_URL"),
                                        it.getString("E_URL")
                                ))
                            } catch (excep: JSONException) {

                            }
                        }
                    }
                }
        Log.d("test", resultList.toString())
        return resultList
    }
}

@Entity(tableName = "areas")
data class Area(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val info: String,
    val picUrl: String,
    val url: String)