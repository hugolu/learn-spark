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
去除表格標頭

```shell
wc -l train.tsv           # 7396
wc -l test.tsv            # 3172

sed '1d' < train.tsv > train-noheader.tsv
sed '1d' < test.tsv > test-noheader.tsv

wc -l train-noheader.tsv  # 7395
wc -l test-noheader.tsv   # 3171
```

## DecisionTreeBinary

### 訓練資料集
```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
```
```scala
val dataRDD = sc.textFile("data/train-noheader.tsv").map(_.replaceAll("\"","")).map(_.split("\t"))
val categoryMap = dataRDD.map(_(3)).distinct.zipWithIndex.collectAsMap

val labeledPointRDD = dataRDD.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(3)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(4, fields.size - 1).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = fields(fields.size - 1).toDouble
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}

val Array(trainRDD, validationRDD) = labeledPointRDD.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
```
```scala
val model = DecisionTree.trainClassifier(trainRDD, 2, Map[Int, Int](), "entropy", 5, 5)
```

### 評估模型
```scala
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
```
```scala
val scoreAndLabels = validationRDD.map{ lp => (model.predict(lp.features), lp.label) }
val matrics = new BinaryClassificationMetrics(scoreAndLabels)
val AUC = matrics.areaUnderROC
//> AUC: Double = 0.6549306810220862
```

### 測試資料集
```scala
val testData = sc.textFile("data/train-noheader.tsv").map(_.replaceAll("\"", "")).map(_.split("\t"))

val testRDD = testData.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(3)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(4, fields.size).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = 0
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}
```

### 預測
```scala
model.predict(testRDD.first.features)           //> res3: Double = 1.0
model.predict(testRDD.map(_.features)).take(5)  //> res4: Array[Double] = Array(1.0, 1.0, 0.0, 0.0, 0.0)
```

## LogisticRegressionWithSGDBiary

### 訓練資料集
因為 Numerical Features 欄位單位不同，數字差異很大，無法彼此比較。需要使用標準化，讓數值特徵欄位有共同的標準。

```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
```
```scala
val dataRDD = sc.textFile("data/train-noheader.tsv").map(_.replaceAll("\"","")).map(_.split("\t"))
val categoryMap = dataRDD.map(_(3)).distinct.zipWithIndex.collectAsMap

val labeledPointRDD = dataRDD.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(3)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(4, fields.size - 1).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = fields(fields.size - 1).toDouble
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}
```
```scala
val stdScaler = new StandardScaler(withMean=true, withStd=true).fit(labeledPointRDD.map(_.features))
val scaledRDD = labeledPointRDD.map(lp => LabeledPoint(lp.label, stdScaler.transform(lp.features)))
val Array(trainRDD, validationRDD) = scaledRDD.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.LogisticRegressionModel
```
```scala
val model = LogisticRegressionWithSGD.train(trainRDD, 5, 50, 0.5)
```

### 評估模型
```scala
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
```
```scala
val scoreAndLabels = validationRDD.map{ lp => (model.predict(lp.features), lp.label) }
val matrics = new BinaryClassificationMetrics(scoreAndLabels)
val AUC = matrics.areaUnderROC
//> AUC: Double = 0.6297526762643042
```

## SVMWithSGD

### 訓練資料集
因為 Numerical Features 欄位單位不同，數字差異很大，無法彼此比較。需要使用標準化，讓數值特徵欄位有共同的標準。

```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
```
```scala
val dataRDD = sc.textFile("data/train-noheader.tsv").map(_.replaceAll("\"","")).map(_.split("\t"))
val categoryMap = dataRDD.map(_(3)).distinct.zipWithIndex.collectAsMap

val labeledPointRDD = dataRDD.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(3)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(4, fields.size - 1).map(field => if (field == "?") 0.0 else field.toDouble)
  val label = fields(fields.size - 1).toDouble
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}
```
```scala
val stdScaler = new StandardScaler(withMean=true, withStd=true).fit(labeledPointRDD.map(_.features))
val scaledRDD = labeledPointRDD.map(lp => LabeledPoint(lp.label, stdScaler.transform(lp.features)))
val Array(trainRDD, validationRDD) = scaledRDD.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.SVMModel
```
```scala
val model = SVMWithSGD.train(trainRDD, 25, 50.0, 1.0)
```

### 評估模型
```scala
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
```
```scala
val scoreAndLabels = validationRDD.map{ lp => (model.predict(lp.features), lp.label) }
val matrics = new BinaryClassificationMetrics(scoreAndLabels)
val AUC = matrics.areaUnderROC
//> AUC: Double = 0.6475482958041099
```

## NaiveBayesBinary

## SVMWithSGD

### 訓練資料集
因為 Numerical Features 欄位單位不同，數字差異很大，無法彼此比較。需要使用標準化，讓數值特徵欄位有共同的標準。此外，Naive Bayes 數值欄位一定要大於 0

```scala
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
```
```scala
val dataRDD = sc.textFile("data/train-noheader.tsv").map(_.replaceAll("\"","")).map(_.split("\t"))
val categoryMap = dataRDD.map(_(3)).distinct.zipWithIndex.collectAsMap

val labeledPointRDD = dataRDD.map{ fields =>
  val categoryFeaturesArray = Array.ofDim[Double](categoryMap.size)
  val categoryIdx = categoryMap(fields(3)).toInt
  categoryFeaturesArray(categoryIdx) = 1
  
  val numericalFeatures = fields.slice(4, fields.size - 1).map(field => if (field == "?") 0.0 else field.toDouble).map(field => if (field < 0) 0.0 else field)
  val label = fields(fields.size - 1).toDouble
  
  LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
}
```
```scala
val stdScaler = new StandardScaler(withMean=false, withStd=true).fit(labeledPointRDD.map(_.features))
val scaledRDD = labeledPointRDD.map(lp => LabeledPoint(lp.label, stdScaler.transform(lp.features)))
val Array(trainRDD, validationRDD) = scaledRDD.randomSplit(Array(0.8, 0.2))
```

### 訓練模型
```scala
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
```
```scala
val model = NaiveBayes.train(trainRDD, 5)
```

### 評估模型
```scala
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
```
```scala
val scoreAndLabels = validationRDD.map{ lp => (model.predict(lp.features), lp.label) }
val matrics = new BinaryClassificationMetrics(scoreAndLabels)
val AUC = matrics.areaUnderROC
//> AUC: Double = 0.6304762672687201
```

# Multi-Class Classification

## UCI CoverType 資料

檔案 | 說明
-----|------
covtype.data | 訓練資料集 (features + label)

欄位
- 0~9: Numerical features, Elevation, Aspect, Slope, ...
- 10~13: Category features, Wilderness Areas
- 14~53: Category features, Soil Types
- 54: label

## DecisionTree Multi-classs Classification
