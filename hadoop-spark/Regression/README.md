# Regression

## 重點整理

## Bike Share 資料集
資料來源: [Bike Sharing Dataset Data Set](http://archive.ics.uci.edu/ml/datasets/bike+sharing+dataset)

idx | 欄位 | 說明 
----|------|------
0 | instant: record index
1 | dteday | date
2 | season | season (1:springer, 2:summer, 3:fall, 4:winter)
3 | yr | year (0: 2011, 1:2012)
4 | mnth | month ( 1 to 12)
5 | hr | hour (0 to 23)
6 | holiday | weather day is holiday or not (extracted from [Web Link])
7 | weekday | day of the week
8 | workingday | if day is neither weekend nor holiday is 1, otherwise is 0.
9 | weathersit | <ul><li>1: Clear, Few clouds, Partly cloudy, Partly cloudy</li><li>2: Mist + Cloudy, Mist + Broken clouds, Mist + Few clouds, Mist</li><li>3: Light Snow, Light Rain + Thunderstorm + Scattered clouds, Light Rain + Scattered clouds</li><li>4: Heavy Rain + Ice Pallets + Thunderstorm + Mist, Snow + Fog</li></ul>
10 | temp | Normalized temperature in Celsius. The values are derived via (t-t_min)/(t_max-t_min), t_min=-8, t_max=+39 (only in hourly scale)
11 | atemp | Normalized feeling temperature in Celsius. The values are derived via (t-t_min)/(t_max-t_min), t_min=-16, t_max=+50 (only in hourly scale)
12 | hum | Normalized humidity. The values are divided to 100 (max)
13 | windspeed | Normalized wind speed. The values are divided to 67 (max)
14 | casual | count of casual users
15 | registered | count of registered users
16 | cnt | count of total rental bikes including both casual and registered

### 訓練資料集
```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
```
```scala
val records = sc.textFile("data/hour-noheader.csv").map(_.split(","))
val data = records.map{ fields =>
  val label = fields.last.toDouble
  val featureSeason = fields.slice(2, 3).map(_.toDouble)
  val features = fields.slice(4, fields.size - 3).map(_.toDouble)
  LabeledPoint(label, Vectors.dense(featureSeason ++ features))
}
val Array(trainRDD, validationRDD) = data.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
```
```scala
val model = DecisionTree.trainRegressor(trainRDD, Map[Int, Int](), "variance", 10, 50)
```

### 評估模型
```scala
import org.apache.spark.mllib.evaluation.RegressionMetrics
```
```scala
val scoreAndLabels = validationRDD.map{ lp => (model.predict(lp.features), lp.label) }
val metrics = new RegressionMetrics(scoreAndLabels)
val RMSE = metrics.rootMeanSquaredError
//> RMSE: Double = 78.02248183419584
```
