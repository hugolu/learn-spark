# 決策樹迴歸分析

## 應用情境
bike sharing 系統 (類似u-bike) 可在某處租借，另一處歸還。他們的管理階層希望運用大數據分析提供更好的服務。

### 分析問題
大數據分析師與營運人員討論後，發現不同天氣狀態下，租用數量差異很大。營運人員無法提供足夠數量、或不知道何時進行維修。因此希望能夠預測某種天氣情況下的租借數量。
- 車需要維修時 - 可以選在租用數量少的時間
- 租用數量大時 - 可以提供更多數量，增加營業額

### 設計模型
可能影響租用數量的因素：

欄位 | 說明
-----|-----
特徵 | 季節、月份、時間(0~23)、假日、星期、工作日、天氣、溫度、體感溫度、濕度、風速
標籤 | 每小時租用數量

### 收集資料
- 每小時租用數量可在 bike sharing 公司電腦系統查詢
- 季節、月份、時間(0~23)、假日、星期、工作日，可從提供天氣歷史資料的公司取得

### 分析資料
大數據分析師決定使用「決策樹回歸分析」(Decision Regression) 來建立模型、訓練、評估、預測資料。

### 其他應用
根據天氣預測的應用很廣泛。例如超商可以根據天氣預測煮關東煮、茶葉蛋、雨衣的銷售量。如果可以預測銷售量，就可以事先準備足夠的數量，增加營業額。也可減少準備太多數量導致浪費食材。

除了超商，餐廳也很適合使用天氣來預測銷售數量。

## Bike Sharing 資料集
資料來源 [Bike Sharing Dataset Data Set](http://archive.ics.uci.edu/ml/datasets/bike+sharing+dataset)

```shell
$ wget http://archive.ics.uci.edu/ml/machine-learning-databases/00275/Bike-Sharing-Dataset.zip
$ unzip Bike-Sharing-Dataset.zip -d data/
Archive:  Bike-Sharing-Dataset.zip
  inflating: data/Readme.txt
  inflating: data/day.csv
  inflating: data/hour.csv
```

hour.csv 欄位介紹

欄位 | 說明 | 處理方式
-----|------|----------
instant     | 序號 ID | 忽略
dteday      | 日期 | 忽略
season      | 氣節 (1:春, 2: 夏, 3: 秋, 4: 冬) | 特徵欄位
yr          | 年份 (0:2011, 1: 2012, ...) | 忽略
mnth        | 月份 (1~12) | 特徵欄位
hr          | 時間 (0~23) | 特徵欄位
holiday     | 假日 (0: 非假日, 1: 假日) | 特徵欄位
weekday     | 星期 | 特徵欄位
workingday  | 工作日 (非週末&非假日) | 特徵欄位
weathersit  | 天氣: <ol><li>Clear, Few clouds, Partly cloudy</li><li>Mist + Cloudy, Mist + Broken clouds, Mist + Few clouds, Mist</li><li>Ligit snow, Light Rain + Thunderstorm + Scattered clouds, Light Rain + Scattered clouds</li><li>Heavy Rain + Ice Pallets + Thunderstorm + Mist Snow + Fog</li></ol> | 特徵欄位
temp        | 攝氏溫度，將除以 41 進行標準化 | 特徵欄位
atemp       | 體感溫度，將溫度除以 50 進行標準化 | 特徵欄位
hum         | 濕度，將濕度除以 100 進行標準化 | 特徵欄位
windspeed   | 風速，將風速除以67進行標準化 | 特徵欄位
casual      | 臨時使用者：此時租借的數目 | 忽略
registered  | 已註冊會員：此時租借的數目 | 忽略
cnt         | 此時租借總數量 cnt = casual + registered | 標籤欄位 (預測目標)

## 建立 RunDecisionRegression.scala

### 執行 RunDecisionRegression
