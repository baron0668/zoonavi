package com.example.zoonavi.model

import android.content.Context
import android.util.Log
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class PlantRepository(context: Context) {
    private val plantDao: PlantDao = AppDatabase.getInstance(context).plantDao()
    private val api = Api()

    init {
//        Plant(
//                0,
//                "九芎",
//                "Lagerstroemia subcostata",
//                "臺灣動物區；蟲蟲探索谷；熱帶雨林區；鳥園區；兩棲爬蟲動物館",
//                "分布於中低海拔森林及長江以南的地區，為台灣的原生樹種。主要生長在潮濕的崩塌地，有吸水保持土壤之特性，所以是良好的水土保持樹種。",
//                "紅褐色的樹皮剝落後呈灰白色，樹幹光滑堅硬。葉有極短的柄，長橢圓形或卵形，全綠，葉片兩端尖，秋冬轉紅。夏季6～8月開花，花冠白色，花數甚多而密生於枝端，花為圓錐花序頂生，花瓣有長柄，邊緣皺曲像衣裙的花邊花絲長短不一。果實為蒴果，橢圓形約6-8公厘，種子有翅。",
//                "\"1. 優良薪炭材：木質堅硬耐燒，是臺灣優良的薪炭材之一。\n" +
//                        "2. 水土保持植栽：可栽植於河岸及邊坡供水土保持。\n" +
//                        "3. 農具用材：木質堅硬，乾燥後不太會反翹，是做農具的用材。\n" +
//                        "4. 食用性：花、根入藥，味淡微苦，敗毒散瘀，花蕾味苦有清香，可生食。葉子是長尾水青蛾幼蟲的食草。\"",
//                "http://www.zoo.gov.tw/iTAP/04_Plant/Lythraceae/subcostata/subcostata_1.jpg",
//                "九芎果實"
//        ).also {
//            GlobalScope.launch {
//                plantDao.insert(it)
//            }
//        }
//
//        Plant(
//                1,
//                "大花紫薇",
//                "Queen lagerstroemia、Queen crape myrtle",
//                "臺灣動物區；蟲蟲探索谷；兒童動物區；熱帶雨林區；白手長臂猿島",
//                "原產於澳洲、熱帶亞洲。生長快速，木材堅硬，耐腐力強，色紅而光亮，價值媲美柚木。",
//                "落葉喬木，可高達25公尺，樹幹通直，樹皮光滑，呈片狀剝落。單葉對生，革質，葉呈長橢圓形，長度10~28公分，寬度5~12公分，脫落前會變紅。花朵直徑5~8公分，花瓣有6枚，長度2.5~3.5公分，接近圓形，邊緣呈不規則波浪狀，早上初開時呈紅色，傍晚轉為紫紅色。果實直徑約3.5公分，呈球形，分裂成6瓣，初生時呈綠色，成熟時轉褐色。種子扁平具有翅，有利隨風散播。",
//                "\"1. 園藝景觀植栽用：花大、美麗，開花時，花團錦簇，甚為壯觀。秋季滿樹金黃的葉子，常栽培於庭園供觀賞或作行道樹用。\n" +
//                        "2. 木材用途：木材堅硬，耐腐力強，色紅而亮，在原產地常被用於家具、舟車、橋樑、電杆、枕木及建築，也作水中用材。\"",
//                "http://www.zoo.gov.tw/iTAP/04_Plant/Lythraceae/speciosa/speciosa_1.jpg",
//                "由數小朵花朵聚成一團甚為壯觀"
//        ).also {
//            GlobalScope.launch {
//                plantDao.insert(it)
//            }
//        }

//        //test
//        GlobalScope.launch {
//            val result = plantDao.getPlantsNameByArea("%臺灣動物區%")
//            result.forEach {
//                Log.d("test", "plant name: $it")
//            }
//
//        }
    }

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
                false
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
                                        it.optString("F_Name_Ch"),
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