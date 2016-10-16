# [NOT YOUR FATHER'S DATABASE: HOW TO USE APACHE SPARK PROPERLY IN YOUR BIG DATA ARCHITECTURE](https://spark-summit.org/east-2016/events/not-your-fathers-database-how-to-use-apache-spark-properly-in-your-big-data-architecture/)

## 優點：一般化處理
- 資料形式：檔案、非結構化資料、社群媒體或網頁資料集、資料備份
- 工作形式：批次、臨時性分析、多步驟 pipeline、交互查詢
- 好處：便宜儲存、擴充彈性、速度與規模
> Pipeline是一系列的Stage按照声明的顺序排列成的工作流

## 缺點：隨機存取
```
sqlContext.sql("select * from my_large_table where id=2134823")
```
- 很沒效率 - spark 可能要搜尋所有檔案才能找到需要的 row

解方: 如果要隨機存取資料，使用資料庫。
- RMDB 使用 index
- Key-Value NoSQL 使用 key 找 value

## 缺點：頻繁插入
```
sqlContext.sql("insert TABLE myTable select fields from my2ndTable")
```
- 每次插入都會創建新檔案 - 創建很快、但未來查詢會很慢

解方：
- 使用資料庫支援插入
- 定期合併(壓縮)小檔案

## 優點：資料轉換 (ETL)
檔案儲存成本很便宜，可以將資料轉換成任意格式 (LOG, Parquet, JSON, CSV, ZIP)

## 缺點：頻繁/定增的更新
不支援更新 (Update) 語法 = 隨機存取 + 刪除 + 插入
- 檔案格式無法對更新動作最佳化

解方：
- 使用資料庫做有效率的更新操作

使用案例：
- Database Snapshot + Incremental SQL Query → Spark
- 秘訣: 使用 ClusterBy 快速合併

## 優點：連接 BI 工具
HDFD ← Spark ← Tableau
- 秘訣: 表格快取優化效能

## 缺點：外部報表
無法同時處理太多請求

解方：
- 將分析結果儲存到資料庫，處理外部查詢

## 優點：機器學習 & 資料科學
使用 MLlib, GraphX, Spark packages，好處
- 內建分散式演算法
