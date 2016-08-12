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
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeMulti --jars lib/joda-time-2.9.4.jar --driver-memory 1024M target/scala-2.10/classification_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 581012 筆
資料分成 trainData: 464671, validationData: 58393, testData = 57948
====== 訓練評估 ======
參數 impurity=gini, maxDepth=3, maxBins=3, AUC=0.660747007346771, time=10415.0
參數 impurity=gini, maxDepth=3, maxBins=5, AUC=0.6786772387101193, time=1771.0
參數 impurity=gini, maxDepth=3, maxBins=10, AUC=0.6737108900039388, time=1815.0
參數 impurity=gini, maxDepth=3, maxBins=50, AUC=0.6768448272909424, time=1682.0
參數 impurity=gini, maxDepth=3, maxBins=100, AUC=0.673847892726868, time=2055.0
參數 impurity=gini, maxDepth=5, maxBins=3, AUC=0.6857328789409689, time=2106.0
參數 impurity=gini, maxDepth=5, maxBins=5, AUC=0.7008888051650026, time=1597.0
參數 impurity=gini, maxDepth=5, maxBins=10, AUC=0.696761598136763, time=1660.0
參數 impurity=gini, maxDepth=5, maxBins=50, AUC=0.6999126607641327, time=2180.0
參數 impurity=gini, maxDepth=5, maxBins=100, AUC=0.7018992002466049, time=2004.0
參數 impurity=gini, maxDepth=10, maxBins=3, AUC=0.7320055486102787, time=3389.0
參數 impurity=gini, maxDepth=10, maxBins=5, AUC=0.7596458479612282, time=3040.0
參數 impurity=gini, maxDepth=10, maxBins=10, AUC=0.7719418423441166, time=3938.0
參數 impurity=gini, maxDepth=10, maxBins=50, AUC=0.7794598667648519, time=4432.0
參數 impurity=gini, maxDepth=10, maxBins=100, AUC=0.775418286438443, time=5475.0
參數 impurity=gini, maxDepth=15, maxBins=3, AUC=0.7792714880208245, time=9038.0
參數 impurity=gini, maxDepth=15, maxBins=5, AUC=0.8201496754748001, time=8900.0
參數 impurity=gini, maxDepth=15, maxBins=10, AUC=0.842429743291148, time=8062.0
參數 impurity=gini, maxDepth=15, maxBins=50, AUC=0.849434007500899, time=13417.0
參數 impurity=gini, maxDepth=15, maxBins=100, AUC=0.8507012826879934, time=19273.0
參數 impurity=gini, maxDepth=20, maxBins=3, AUC=0.803846351446235, time=16745.0
參數 impurity=gini, maxDepth=20, maxBins=5, AUC=0.8580823043857997, time=21122.0
參數 impurity=gini, maxDepth=20, maxBins=10, AUC=0.8867501241587177, time=24357.0
參數 impurity=gini, maxDepth=20, maxBins=50, AUC=0.9061702601339202, time=39257.0
參數 impurity=gini, maxDepth=20, maxBins=100, AUC=0.9031904509102119, time=73472.0
參數 impurity=entropy, maxDepth=3, maxBins=3, AUC=0.6564142962341376, time=1618.0
參數 impurity=entropy, maxDepth=3, maxBins=5, AUC=0.6652509718630658, time=1198.0
參數 impurity=entropy, maxDepth=3, maxBins=10, AUC=0.6700118164848526, time=1503.0
參數 impurity=entropy, maxDepth=3, maxBins=50, AUC=0.6687616666381244, time=1377.0
參數 impurity=entropy, maxDepth=3, maxBins=100, AUC=0.6687445412977583, time=1529.0
參數 impurity=entropy, maxDepth=5, maxBins=3, AUC=0.6881989279536931, time=1883.0
參數 impurity=entropy, maxDepth=5, maxBins=5, AUC=0.6971554809651842, time=1643.0
參數 impurity=entropy, maxDepth=5, maxBins=10, AUC=0.6939701676570822, time=1902.0
參數 impurity=entropy, maxDepth=5, maxBins=50, AUC=0.696521843371637, time=1725.0
參數 impurity=entropy, maxDepth=5, maxBins=100, AUC=0.7012826879934239, time=2206.0
參數 impurity=entropy, maxDepth=10, maxBins=3, AUC=0.7237682598941654, time=2970.0
參數 impurity=entropy, maxDepth=10, maxBins=5, AUC=0.759748600003425, time=2979.0
參數 impurity=entropy, maxDepth=10, maxBins=10, AUC=0.7628140359289641, time=2760.0
參數 impurity=entropy, maxDepth=10, maxBins=50, AUC=0.7731577415101125, time=4243.0
參數 impurity=entropy, maxDepth=10, maxBins=100, AUC=0.7706231911359238, time=4557.0
參數 impurity=entropy, maxDepth=15, maxBins=3, AUC=0.7711883273680065, time=6423.0
參數 impurity=entropy, maxDepth=15, maxBins=5, AUC=0.8193276591372254, time=7422.0
參數 impurity=entropy, maxDepth=15, maxBins=10, AUC=0.8452725497919271, time=7829.0
參數 impurity=entropy, maxDepth=15, maxBins=50, AUC=0.8554963779905126, time=13104.0
參數 impurity=entropy, maxDepth=15, maxBins=100, AUC=0.8523795660438751, time=21307.0
參數 impurity=entropy, maxDepth=20, maxBins=3, AUC=0.8014316784546093, time=14684.0
參數 impurity=entropy, maxDepth=20, maxBins=5, AUC=0.8593667049132602, time=18259.0
參數 impurity=entropy, maxDepth=20, maxBins=10, AUC=0.89414827119689, time=17905.0
參數 impurity=entropy, maxDepth=20, maxBins=50, AUC=0.913499905810628, time=28013.0
參數 impurity=entropy, maxDepth=20, maxBins=100, AUC=0.9073690339595499, time=42107.0
最佳參數 impurity=entropy, maxDepth=20, maxBins=50, AUC=0.913499905810628
====== 測試模型 ======
測試最佳模型，結果 AUC=0.9124042244771174
====== 預測資料 ======
共計 581012 筆
土地條件 海拔:2595.0, 方位:45.0, 斜率:＄2.0, 水源垂直距離:153.0, 水源水平距離:-1.0, 9點時陰影:391.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2606.0, 方位:45.0, 斜率:＄7.0, 水源垂直距離:270.0, 水源水平距離:5.0, 9點時陰影:633.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2609.0, 方位:214.0, 斜率:＄7.0, 水源垂直距離:150.0, 水源水平距離:46.0, 9點時陰影:771.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2511.0, 方位:92.0, 斜率:＄7.0, 水源垂直距離:182.0, 水源水平距離:18.0, 9點時陰影:722.0 => 預測:1.0, 實際:4.0, 結果:錯誤
土地條件 海拔:2533.0, 方位:71.0, 斜率:＄9.0, 水源垂直距離:150.0, 水源水平距離:-3.0, 9點時陰影:577.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2529.0, 方位:68.0, 斜率:＄8.0, 水源垂直距離:210.0, 水源水平距離:-5.0, 9點時陰影:666.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2795.0, 方位:79.0, 斜率:＄10.0, 水源垂直距離:531.0, 水源水平距離:96.0, 9點時陰影:2980.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2847.0, 方位:352.0, 斜率:＄26.0, 水源垂直距離:150.0, 水源水平距離:82.0, 9點時陰影:3697.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2840.0, 方位:14.0, 斜率:＄14.0, 水源垂直距離:216.0, 水源水平距離:88.0, 9點時陰影:3552.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2752.0, 方位:332.0, 斜率:＄6.0, 水源垂直距離:342.0, 水源水平距離:24.0, 9點時陰影:2372.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2569.0, 方位:102.0, 斜率:＄7.0, 水源垂直距離:228.0, 水源水平距離:18.0, 9點時陰影:1266.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2982.0, 方位:53.0, 斜率:＄14.0, 水源垂直距離:240.0, 水源水平距離:63.0, 9點時陰影:5756.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2929.0, 方位:356.0, 斜率:＄12.0, 水源垂直距離:0.0, 水源水平距離:0.0, 9點時陰影:5757.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2886.0, 方位:12.0, 斜率:＄7.0, 水源垂直距離:570.0, 水源水平距離:94.0, 9點時陰影:4295.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2837.0, 方位:112.0, 斜率:＄8.0, 水源垂直距離:272.0, 水源水平距離:16.0, 9點時陰影:3649.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2591.0, 方位:42.0, 斜率:＄12.0, 水源垂直距離:350.0, 水源水平距離:22.0, 9點時陰影:1398.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:3034.0, 方位:189.0, 斜率:＄14.0, 水源垂直距離:384.0, 水源水平距離:66.0, 9點時陰影:5754.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:3175.0, 方位:15.0, 斜率:＄7.0, 水源垂直距離:366.0, 水源水平距離:92.0, 9點時陰影:4816.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:3077.0, 方位:129.0, 斜率:＄3.0, 水源垂直距離:618.0, 水源水平距離:43.0, 9點時陰影:6296.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:3047.0, 方位:28.0, 斜率:＄11.0, 水源垂直距離:600.0, 水源水平距離:13.0, 9點時陰影:6414.0 => 預測:0.0, 實際:0.0, 結果:正確
===== 完成 ======
```
