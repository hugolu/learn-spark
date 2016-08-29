# 回歸模型

分類與回歸模型原則類似，都是通過確定一個模型，將輸入特徵映射到預測的輸出
- 分類模型處理類別的離散變量
- 回歸模型處理任意實數的目標變量

回歸模型的例子
- 預測股票收益和其他經濟相關的因素
- 預測貸款違約造成的損失 (與分類模型結合，分類模型處理違約機率、回歸模型預測違約損失)
- 推薦系統 (ALS 在每次迭代時都使用的線性回歸)
- 基於用戶的行爲和消費模式，預測顧客對於零售、移動或者其他商業型態的存在價值

## 回歸模型的種類
mllib 提供兩大回歸模型：線性模型與決策樹模型

### 最小平方回歸 (Latest Squares Regression)
- 預測函數: y = w<sup>T</sup>x
- 損失函數: 1/2 (w<sup>T</sup>x - y)<sup>2</sup>
  - y: 目標變量
  - w: 權重變量
  - x: 特徵向量

在 mllib 中，標準的最小平方回歸不使用正則化。但應用到錯誤的預測值的損失函數會將錯誤做平方，從而放大。這意味著最小平方回歸對數據中的異常點和 overfitting 非常敏感。因此對於分類器，通常在實際中必須應用一定程度的**正則化 (regularization)**。

### 決策樹回歸 (Decision Tree Regression)
類似線性回歸模型需要適用對應損失函數，決策樹在用於回歸時也要使用對應的不純度量方法。這裏的不純度量方法是**方差**，和最小平方回歸模型定義方差損失的方式一樣。

> 本書作者在這裡輕輕帶過理論，說實在我看不懂上面寫的東西，只好之後再參考其他文件補上這個落差

## Spark 建構回歸模型
回歸模型與分類模型基礎一樣，使用相同的方法處理輸入的特徵。唯一不同的是，回歸模型預測目標是實數變量，分類模型預測目標是類別編號。

使用 Bike share 數據及作為回歸分析的實驗資料，從 [
Bike Sharing Dataset Data Set](http://archive.ics.uci.edu/ml/datasets/bike+sharing+dataset) 下載相關數據
```shell
$ wget http://archive.ics.uci.edu/ml/machine-learning-databases/00275/Bike-Sharing-Dataset.zip
$ unzip Bike-Sharing-Dataset.zip
Archive:  Bike-Sharing-Dataset.zip
  inflating: Readme.txt
  inflating: day.csv
  inflating: hour.csv
```

Readme.txt 說明數據集相關資訊

| idx | 名稱 | 說明 |
|-----|------|------|
| 0   | instant | 紀錄 ID |
| 1   | dteday | 時間 |
| 2   | season | 季節 (1:springer, 2:summer, 3:fall, 4:winter) |
| 3   | yr | 年份 (0: 2011, 1:2012) |
| 4   | mnth | 月份 ( 1 to 12) |
| 5   | hr | 時刻 (0 to 23) |
| 6   | holiday | 是否是假日  |
| 7   | weekday | 週幾 |
| 8   | workingday | 當天是否工作日 |
| 9   | weathersit | 天氣類型參數 |
| 10  | temp | 氣溫 |
| 11  | atemp |  體感溫度 |
| 12  | hum | 濕度 |
| 13  | windspeed | 風速 |
| 14  | casual     | 臨時使用者數量 |
| 15  | registered | 註冊使用者數量 |
| 16  | cnt | 目標變量，每小時的自行車租用量 |

去掉標頭
```shell
$ sed 1d hour.csv > hour_noheader.csv
```

## 從數據中抽取合適的特徵

## 回歸模型的訓練與應用

## 評估回歸模型的性能

## 改進模型性能與參數調優
