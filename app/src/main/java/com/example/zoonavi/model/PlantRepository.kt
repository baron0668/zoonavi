package com.example.zoonavi.model

import android.content.Context
import android.util.Log
import androidx.room.*
import com.example.zoonavi.viewmodel.Callback
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class PlantRepository(context: Context, val callback: Callback) {
    private val plantDao: PlantDao = AppDatabase.getInstance(context).plantDao()
    private val api = Api()

    suspend fun updatePlantsDb(): Boolean {
        return withContext(Dispatchers.IO) {
            val apiResult = api.sendRequest(Api.Source.Plant)
            if (apiResult != null) {
                plantDao.delete()
                parseJson(apiResult).forEach {
                    plantDao.insert(it)
                }
                true
            } else {
                plantDao.delete()
                readOfflineData().forEach {
                    plantDao.insert(it)
                }
                true
            }
        }
    }

    suspend fun findPlants(areaName: String): List<Plant> {
        return withContext(Dispatchers.IO) {
            ArrayList<Plant>().also {
                it.addAll(plantDao.getPlantsByArea(areaName))
            }
        }
    }

    private fun readOfflineData(): List<Plant> {
        val resultList: MutableList<Plant> = ArrayList()
        val csvReader = CSVReader(callback.getAssetReader("plants.csv"))
        val fieldNameArray = csvReader.readNext()
        val fieldIndexArray = Array<Int>(10) {
            fieldNameArray.indexOf(when(it) {
                0 -> "F_Name_Ch"
                1 -> "F_Name_En"
                2 -> "F_AlsoKnown"
                3 -> "F_Location"
                4 -> "F_Brief"
                5 -> "F_Feature"
                6 -> "F_Function&Application"
                7 -> "F_Update"
                8 -> "F_Pic01_URL"
                else -> "F_Pic01_ALT"
            })
        }
        var index = 0
        csvReader.readAll().forEach {
            resultList.add(Plant(
                index,
                it[fieldIndexArray[0]],
                it[fieldIndexArray[1]],
                it[fieldIndexArray[2]],
                it[fieldIndexArray[3]],
                it[fieldIndexArray[4]],
                it[fieldIndexArray[5]],
                it[fieldIndexArray[6]],
                it[fieldIndexArray[7]],
                it[fieldIndexArray[8]],
                it[fieldIndexArray[9]]
            ))
            index++
        }
        csvReader.close()
        return resultList
    }

    private fun parseJson(jsonString: String): List<Plant> {
        val resultList: MutableList<Plant> = ArrayList()
        JSONObject(jsonString)
            .optJSONObject("result")
            ?.optJSONArray("results")?.also {resultsArray ->
                for (i in 0 until resultsArray.length()) {
                    resultsArray.getJSONObject(i).also {
                        try {
                            resultList.add(
                                Plant(
                                        it.getInt("_id"),
                                        it.getString("F_Name_Ch"),
                                        it.getString("F_Name_En"),
                                        it.getString("F_AlsoKnown"),
                                        it.getString("F_Location"),
                                        it.getString("F_Brief"),
                                        it.getString("F_Feature"),
                                        it.getString("F_Function＆Application"),
                                        it.getString("F_Update"),
                                        it.getString("F_Pic01_URL"),
                                        it.getString("F_Pic01_ALT")
                                )
                            )
                        } catch (excep: JSONException) {
                            Log.d("test", excep.toString())
                        }
                    }
                }
            }
        Log.d("test", resultList.toString())
        return resultList
    }

}

@Entity(tableName = "plants")
data class Plant(
        @PrimaryKey val id: Int,
        val name: String,
        val nameInEng: String,
        val alsoKnown: String,
        val location: String,
        val briefInfo: String,
        val featureInfo: String,
        val applicationInfo: String,
        val updateDate: String,
        val mainPicUrl: String,
        val mainPicInfo: String,
)

@Dao
interface PlantDao {
    @Query("SELECT name FROM plants WHERE location LIKE :areaName ORDER BY id")
    suspend fun getPlantsNameByArea(areaName: String): Array<String>

    @Query("SELECT * FROM plants WHERE location LIKE :areaName ORDER BY id")
    suspend fun getPlantsByArea(areaName: String): Array<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: Plant)

    @Query("DELETE FROM plants")
    suspend fun delete()
}