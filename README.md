## 編譯環境
- SDK api 29
- android studio v4.1.1
- kotlin v1.4.21
## 程式架構
- MVVM
- use viewbinding
## 函式庫
- SQLite Room
- Okhttp3
- Glide (image loader)
- openCSV (reading csv files)
## 其他說明
1. 相關資料載入後會存入快取，所以開啟app後都只會做一次資料載入的動作
2. 園區資料是memory cache。植物資料因數量較多，為避免佔用memory所以先存入Database
3. 可使用橫向模式，但只是為了測試，並沒有針對橫向去優化Layout
4. 測試無網路狀態的狀態時。因有cache，所以須先將背景程式移除後重新啟動App。
5. Api無法取得資料時，會從App中的csv檔案讀取
6. 可測試Activity被系統移除的狀況。在任何頁面下皆會檢查資料是否正確載入
## 加分項目
- 加入載入動畫、頁面切換動畫
