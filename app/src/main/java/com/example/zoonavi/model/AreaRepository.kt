package com.example.zoonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AreaRepository {
    suspend fun getAreaList(): List<Area> {
        return withContext(Dispatchers.IO) {
            //simulate delay
            Thread.sleep(500)
            ArrayList<Area>(2).apply {
                this.add(Area(
                    1,
                    "臺灣動物區",
                    "戶外區",
                    "臺灣動物區以臺灣原生動物與棲息環境為展示重點，佈置模擬動物原生棲地之生態環境，讓動物表現如野外般自然的生活習性，引導觀賞者更正確地認識本土野生動物。臺灣位處於亞熱帶，雨量充沛、氣候溫暖，擁有各種地形景觀，因而孕育了豐富龐雜的生物資源。",
                    "http://www.zoo.gov.tw/iTAP/05_Exhibit/01_FormosanAnimal.jpg",
                    "http://www.zoo.gov.tw/introduce/gq.aspx?tid=12"
                ))
                this.add(Area(
                    2,
                    "兒童動物區",
                    "戶外區",
                    "兒童動物園全區以埤塘、水田等各類濕地與郊野生態造景為環境意象，串聯農村動物、經濟動物、寵物、入侵之外來種動物等單元主題，點出人類與動物間的密切關係，提供學童、家長與老師一處共同體驗與學習的空間。",
                    "http://www.zoo.gov.tw/iTAP/05_Exhibit/02_ChildrenZoo.jpg",
                    "http://www.zoo.gov.tw/introduce/gq.aspx?tid=13"
                ))
            }
        }
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