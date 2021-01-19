package com.example.zoonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zoonavi.viewmodel.Callback
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class AreaRepository(val callback: Callback) {
    private val api: Api = Api()
    private var dataList: MutableList<Area>? = null

    suspend fun getAreaList(): List<Area>? {
        return withContext(Dispatchers.IO) {
            if (dataList == null) {
                val apiResult = api.sendRequest(Api.Source.Area)
                dataList = if (apiResult != null) {
                    ArrayList(parseJson(apiResult))
                } else {
                    ArrayList(readFromOfflineData())
                }
            }
            dataList
        }
    }

    private suspend fun readFromOfflineData(): List<Area> {
        val resultList: MutableList<Area> = ArrayList()
        val csvReader = CSVReader(callback.getAssetReader("areas.csv"))
        val fieldNameArray = csvReader.readNext()
        val fieldIndexArray = Array<Int>(7) {
            fieldNameArray.indexOf(when(it) {
                0 -> "E_no"
                1 -> "E_Name"
                2 -> "E_Category"
                3 -> "E_Info"
                4 -> "E_Pic_URL"
                5 -> "E_URL"
                else -> "E_Geo"
            })
        }
        csvReader.readAll().forEach {
            resultList.add(Area(
                it[fieldIndexArray[0]].toInt(),
                it[fieldIndexArray[1]],
                it[fieldIndexArray[2]],
                it[fieldIndexArray[3]],
                it[fieldIndexArray[4]],
                it[fieldIndexArray[5]],
                parseGeoInfo(it[fieldIndexArray[6]])
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
                                        it.getString("E_URL"),
                                        parseGeoInfo(it.getString("E_Geo"))
                                ))
                            } catch (excep: JSONException) {

                            }
                        }
                    }
                }
        return resultList
    }

    private fun parseGeoInfo(infoString: String): DoubleArray  {
        val result = DoubleArray(2)
        val matcher = Pattern.compile("MULTIPOINT \\(\\(([0-9]+.[0-9]+) ([0-9]+.[0-9]+)\\)\\)").matcher(infoString)
        if (matcher.find()) {
            result[1] = matcher.group(1)?.toDouble() ?: 0.0
            result[0] = matcher.group(2)?.toDouble() ?: 0.0
        }
        return result
    }
}

@Entity(tableName = "areas")
data class Area(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val info: String,
    val picUrl: String,
    val url: String,
    val geo: DoubleArray,
    var distance: Int = -1)