# 決策數多元分類

## 應用情境
森林管理員希望能運用大數據分析，更節省人力、經費來管理森林，提高森林覆蓋率。

### 定義問題
一座森林中有各種樹種，如熱帶、針葉林...，每塊土地都有適合生長的樹種。希望能預測哪些土地適合哪些樹種，就能在適合的土地種植適合生長的植物。

### 設計模型
海拔、方位、斜率、水源的垂直距離、荒野分類、水源的水平距離、土壤分類等，都會影響適合生長的樹種，整理資料如下表格：

欄位 | 說明
-----|-----
特徵 | 海拔、方位、斜率、水源垂直距離、荒野分類、水源水平距離、土壤分類
標籤 |  <ol><li>Spruce/Fir</li><li>Lodgepole Pine</li><li>Ponderosa Pine</li><li>Cottonwood/Willon</li><li>Aspen</li><li>Douglas-fir</li><li>Krummholz</li></ul>

### 搜集資料
透過森林管理單位長期田野調查，借助現代科技如空拍機、衛星圖，搜集這些資料。

### 分析資料
使用 「決策樹多元分析」(Decision Tree Multi-Class Classification) 建立模型、訓練、評估、預測資料。

### 其他應用情境
研究什麼地點適合開設什麼樣類型的店面:
- 特徵：與捷運站距離、與大學距離、與中學距離、該地點平均年收入、馬路寬度、附近人口數、門口每小時人流量
- 標籤：配合實際調查該地點店面類型，已經三年以上且賺錢的店面類型 (表示適合的店面類型)

## UCI Covertype 資料集介紹

### Machine Learning Repository
Machine Learning Repository 是加州大學爾灣分校 (University of California Irvine) 提供用於研究機器學習的數據庫。
- http://archive.ics.uci.edu/ml/

### UCI Covertype 資料集
Covertype Dataset 森林覆蓋樹種資料集，包含一座森林中有各種樹種的相關資訊。
- http://archive.ics.uci.edu/ml/datasets/Covertype

```shell
$ wget http://archive.ics.uci.edu/ml/machine-learning-databases/covtype/covtype.data.gz
$ gzip -d covtype.data.gz
$ mv covtype.data data/
```

欄位  | 分類 | 說明 
------|------|------
1~10  | 數值特徵 | 海拔、方位、斜率、水源垂直距離、水源水平距離、九點時陰影
11~14 | 分類特徵 | 荒野分類(1-of-k encoding): <ol><li>Rawah Wilderness Area</li><li>Neota Wilderness Area</li><li>Comanche Peak Wilderness Area</li><li>Cache la Proudre Wilderness Area</li></ol>
15~54 | 分類特徵 | 土壤分類(1-of-k encoding):
55    | 標籤欄位 | 森林覆蓋分類: <ol><li>Spruce/Fir</li><li>Lodgepole Pine</li><li>Ponderosa Pine</li><li>Cottonwood/Willon</li><li>Aspen</li><li>Douglas-fir</li><li>Krummholz</li></ul>

> 1-of-k encoding: 如果是 Neota Wilderness Area 則為 0,1,0,0，如果是 Comanche Peak Wilderness Area 則為 0,0,1,0

## 建立 RunDecisionTreeMulti.scala
```shell
$ vi RunDecisionTreeMulti.scala
```

RunDecisionTreeMulti.scala:
```scala
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd._
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.joda.time.format._
import org.joda.time._
import org.joda.time.Duration

object RunDecisionTreeMulti {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("DecisionTreeMulti").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData, Array("gini", "entropy"), Array(3,5,10,15,20), Array(3,5,10,50,100))

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    PredictData(sc, model)

    println("===== 完成 ======")
    trainData.unpersist()
    validationData.unpersist()
    testData.unpersist()
  }

  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint]) = {
    //-- 1. 匯入、轉換資料
    println("開始匯入資料")

    val rawData = sc.textFile("data/covtype.data")
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立 RDD[LabeledPoint]
    val labelpointRDD = rawData.map{ record =>
      val fields = record.split(',').map(_.toDouble)
      val label = fields.last - 1
      LabeledPoint(label, Vectors.dense(fields.init))
    }

    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))
    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurityArray: Array[String], maxDepthArray: Array[Int], maxBinsArray: Array[Int]): DecisionTreeModel = {
    val evaluationsArray = for {
      impurity <- impurityArray
      maxDepth <- maxDepthArray
      maxBins <- maxBinsArray
    } yield {
      val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
      val auc = evaluateModel(model, validationData)
      println(s"參數 impurity=$impurity, maxDepth=$maxDepth, maxBins=$maxBins, AUC=$auc, time=$time")

      (impurity, maxDepth, maxBins, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    println(s"最佳參數 impurity=${bestEval._1}, maxDepth=${bestEval._2}, maxBins=${bestEval._3}, AUC=${bestEval._4}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)

    model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Double) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainClassifier(trainData, 7, Map[Int, Int](), impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new MulticlassMetrics(scoreAndLabels)
    val precision = metrics.precision
    precision
  }

  def PredictData(sc: SparkContext, model: DecisionTreeModel) = {
    //-- 1. 匯入並轉換資料
    val rawData = sc.textFile("data/covteyp.data")
    println(s"共計 ${rawData.count} 筆")

    //-- 2. 建立預測所需資料
    val Array(pData, oData) = rawData.randomSplit(Array(0.1, 0.9))
    val data = pData.take(20).map { record => 
      val fields = record.split(',').map(_.toDouble)
      val features = Vectors.dense(fields.init)
      val label = fields.last - 1
      val predict = model.predict(features)
      val result = if (label == predict) "正確" else "錯誤"

      println(s"土地條件 海拔:${features(0)}, 方位:${features(1)}, 斜率:＄${features(2)}, 水源垂直距離:${features(3)}, 水源水平距離:${features(4)}, 9點時陰影:${features(5)} => 預測:${predict}, 實際:${label}, 結果:${result}")
    }
  }

}
```

### 執行 RunDecisionTreeMuti
