# Binary Classification

## 重點整理

## StumbleUpon 資料

檔案 | 說明
-----|------
train.tsv | 訓練資料集 (features + label)
test.tsv  | 測試資料集 (features)

欄位
- 0~2: 網址、ID、樣板文字 (don't use)
- 3: 網頁分類 (string)
- 4~25: 其他特徵 (int, double)
- 26: label

### 預處理
```shell
wc -l train.tsv           # 7396
wc -l test.tsv            # 3172

sed '1d' < train.tsv > train-noheader.tsv
sed '1d' < test.tsv > test-noheader.tsv

wc -l train-noheader.tsv  # 7395
wc -l test-noheader.tsv   # 3171
```

### 訓練資料集
```scala
val trainData = sc.textFile("data/train-noheader.tsv").map(line => line.replaceAll("\"","")).map(_.split("\t").drop(3))

val categoryMap = trainData.map(_(0)).distinct.zipWithIndex.collectAsMap

//val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
//categoryFeaturesArray(categoryMap("business").toInt) = 1

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
val trainLabeledPoint = trainData.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(0)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(2, fields.size - 1).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = fields(fields.size - 1).toInt
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}

val Array(trainRDD, validationRDD) = trainLabeledPoint.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.tree.DecisionTree
//import org.apache.spark.mllib.tree.model.DecisionTreeModel

val model = DecisionTree.trainClassifier(trainLabeledPoint, 2, Map[Int, Int](), "entropy", 5, 5)
```

### 評估模型
```scala
validationRDD.first.label                     //> res1: Double = 1.0
model.predict(validationRDD.first.features)   //> res2: Double = 0.0

model.predict(validationRDD.map(lp => lp.features)).take(5)
//> res3: Array[Double] = Array(0.0, 0.0, 1.0, 0.0, 1.0)
```

```scala
val scoreAndLabels = validationRDD.map{ lp =>
  val predict = model.predict(lp.features)
  (predict, lp.label)
}

import org.apache.spark.mllib.evaluation._
val matrics = new BinaryClassificationMetrics(scoreAndLabels)
val AUC = matrics.areaUnderROC
AUC: Double = 0.6549306810220862
```

### 測試資料集
```scala
val testData = sc.textFile("data/train-noheader.tsv").map(line => line.replaceAll("\"", "")).map(_.split("\t").drop(3))

val testRDD = trainData.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(0)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(2, fields.size).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = 0
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}
```

### 預測
```scala
testRDD.take(5).foreach{ lp => println(model.predict(lp.features)) }
```
