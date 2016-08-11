# 邏輯迴歸二元分類

## 羅輯回歸分析介紹

### 簡單迴歸分析 (Simple Regression Analysis)
假設變數 y 是自變數 x 所構成的某種現性函數，再加上一個誤差值所得到的值: y = b<sub>0</sub> + b<sub>1</sub>x

如果線性迴歸中「應變數」不是連續變項，而是二分變項，例如[是][否]得到疾病，就要使用 logistic regression。

將 y = b<sub>0</sub> + b<sub>1</sub>x 轉換成 Sigmoid 函數，用來界定某個資料的類別: 

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/a26a3fa3cbb41a3abfe4c7ff88d47f0181489d13)
其中 t = b<sub>0</sub> + b<sub>1</sub>x

![](https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Logistic-curve.svg/600px-Logistic-curve.svg.png)

透過 sigmoid 算出來的值 p (probability) 如果大於 0.5，則歸類為「是」，反之則為「否」

### 複迴歸 (Multiple Regression Analysis)
複迴歸使用超過一個自變量，公式: y = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

轉換成 Sigmoid 函數，t = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

## LogisticRegression 專案
```shell
$ mkdir LogisticRegression
$ cd cd LogisticRegression/
$ mkdir data
$ cp /vagrant/train.tsv /vagrant/test.tsv data/   # 複製訓練、評估資料
$ mkdir lib
$ cp /vagrant/joda-time-2.9.4.jar lib/            # 複製相依套件
```

### RunLogisticRegressionWithSGDBinary.scala
```shell
$ mkdir -p src/main/scala/
$ vi src/main/scala/RunLogisticRegressionWithSGDBinary.scala
```

RunLogisticRegressionWithSGDBinary.scala:
```scala
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.joda.time._

object RunLogisticRegerssionWithSGDBinary {
  def main(args: Array[String]) {
    SetLogger

    val sc = new SparkContext(new SparkConf().setAppName("LogisticRegerssion").setMaster("local[4]"))

    println("====== 準備階段 ======")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist()
    validationData.persist()
    testData.persist()

    println("====== 訓練評估 ======")
    val model = trainEvaluateTunning(trainData, validationData, Array(5,15,20,60,100), Array(10,50,100,200), Array(0.5, 0.8, 1))

    println("====== 測試模型 ======")
    val auc = evaluateModel(model, testData)
    println(s"測試最佳模型，結果 AUC=${auc}")

    println("====== 預測資料 ======")
    PredictData(sc, model, categoriesMap)

    println("===== 完成 ======")
  }


  def SetLogger = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("com").setLevel(Level.OFF)
    System.setProperty("spark.ui.showConsoleProgress", "false")
    Logger.getRootLogger().setLevel(Level.OFF)
  }

  def PrepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint], Map[String, Int]) = {
    //-- 1. 匯入、轉換資料
    println("開始匯入資料")

    val rawDataWithHeader = sc.textFile("data/train.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    val lines = rawData.map(_.split("\t"))
    println(s"共計 ${lines.count} 筆")

    //-- 2. 建立 RDD[LabeledPoint]
    val categoriesMap = lines.map{ fields => fields(3)}.distinct.collect().zipWithIndex.toMap
    val labelpointRDD = lines.map{ fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val categoryIdx = categoriesMap(fields(3))
      categoryFeaturesArray(categoryIdx) = 1
      val numericalFeatures = trFields.slice(4, fields.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
      val label = trFields(fields.size - 1).toInt
      LabeledPoint(label, Vectors.dense(categoryFeaturesArray ++ numericalFeatures))
    }

    //-- 進行資料標準化
    val featuresData = labelpointRDD.map(labelpoint => labelpoint.features)
    val stdScaler = new StandardScaler(withMean = true, withStd = true).fit(featuresData)
    val scaledRDD = labelpointRDD.map(labelpoint => LabeledPoint(labelpoint.label, stdScaler.transform(labelpoint.features)))


    //-- 3. 以隨機方式將資料份成三份
    val Array(trainData, validationData, testData) = scaledRDD.randomSplit(Array(0.8, 0.1, 0.1))

    println(s"資料分成 trainData: ${trainData.count}, validationData: ${validationData.count}, testData = ${testData.count}")

    (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluateTunning(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], numIterationsArray: Array[Int], stepSizeArray: Array[Int], miniBatchFractionArray: Array[Double]): LogisticRegressionModel = {
    val evaluationsArray = for {
      numIterations <- numIterationsArray
      stepSize <- stepSizeArray
      miniBatchFraction <- miniBatchFractionArray
    } yield {
      val (model, time) = trainModel(trainData, numIterations, stepSize, miniBatchFraction)
      val auc = evaluateModel(model, validationData)
      println(s"參數 numIterations=$numIterations, stepSize=$stepSize, miniBatchFraction=$miniBatchFraction, AUC=$auc, time=$time")

      (numIterations, stepSize, miniBatchFraction, auc)
    }

    val bestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    println(s"最佳參數 numIterations=${bestEval._1}, stepSize=${bestEval._2}, miniBatchFraction=${bestEval._3}, AUC=${bestEval._4}")

    val (model, time) = trainModel(trainData.union(validationData), bestEval._1, bestEval._2, bestEval._3)

    model
  }

  /*
  def trainEvaluate(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint]): LogisticRegressionModel = {
    println("開始訓練...")

    val (model, time) = trainModel(trainData, "entropy", 10, 10)
    println(s"訓練完成 所需時間:${time}ms")

    val auc = evaluateModel(model, validationData)
    println(s"評估結果 AUC=${auc}")
    
    model
  }
*/

  def trainModel(trainData: RDD[LabeledPoint], numIterations: Int, stepSize: Double, miniBatchFraction: Double): (LogisticRegressionModel, Double) = {
    val startTime = new DateTime()
    val model = LogisticRegressionWithSGD.train(trainData, numIterations, stepSize, miniBatchFraction)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)

    (model, duration.getMillis)
  }

  def evaluateModel(model: LogisticRegressionModel, validationData: RDD[LabeledPoint]): Double = {
    val scoreAndLabels = validationData.map { data =>
      var predict = model.predict(data.features)
      (predict, data.label)
    }
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auc = metrics.areaUnderROC
    auc
  }

  def PredictData(sc: SparkContext, model: LogisticRegressionModel, categoriesMap: Map[String, Int]) = {
    //-- 1. 匯入並轉換資料
    val rawDataWithHeader = sc.textFile("data/test.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex{ (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    val lines = rawData.map(_.split("\t"))
    println(s"共計 ${lines.count} 筆")

    //-- 2. 建立預測所需資料
    val dataRDD = lines.take(10).map{ fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val categoryIdx = categoriesMap(fields(3))
      categoryFeaturesArray(categoryIdx) = 1
      val numericalFeature = trFields.slice(4, fields.size).map(d => if (d == "?") 0.0 else d.toDouble)
      val label = 0

      //-- 3. 進行預測
      val url = trFields(0)
      val Features = Vectors.dense(categoryFeaturesArray ++ numericalFeature)
      val predict = model.predict(Features).toInt
      val predictDesc = predict match {
        case 0 => "暫時性網頁"
        case 1 => "長青網頁"
      }
      
      println(s"網址 ${url} ==> 預測: ${predictDesc}")
    }
  }

}
```

```shell
$ spark-submit --class RunLogisticRegerssionWithSGDBinary --jars lib/joda-time-2.9.4.jar target/scala-2.10/logisticregerssion_2.10-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 7395 筆
資料分成 trainData: 5946, validationData: 730, testData = 719
====== 訓練評估 ======
參數 numIterations=5, stepSize=10, miniBatchFraction=0.5, AUC=0.6545084521744354, time=2752.0
參數 numIterations=5, stepSize=10, miniBatchFraction=0.8, AUC=0.6530478142999827, time=552.0
參數 numIterations=5, stepSize=10, miniBatchFraction=1.0, AUC=0.6572494949722516, time=412.0
參數 numIterations=5, stepSize=50, miniBatchFraction=0.5, AUC=0.6500063832503511, time=426.0
參數 numIterations=5, stepSize=50, miniBatchFraction=0.8, AUC=0.6350808419882699, time=417.0
參數 numIterations=5, stepSize=50, miniBatchFraction=1.0, AUC=0.6363011692612702, time=422.0
參數 numIterations=5, stepSize=100, miniBatchFraction=0.5, AUC=0.6499463055999881, time=328.0
參數 numIterations=5, stepSize=100, miniBatchFraction=0.8, AUC=0.6296588340430007, time=325.0
參數 numIterations=5, stepSize=100, miniBatchFraction=1.0, AUC=0.6347804537364543, time=309.0
參數 numIterations=5, stepSize=200, miniBatchFraction=0.5, AUC=0.6486058230262615, time=605.0
參數 numIterations=5, stepSize=200, miniBatchFraction=0.8, AUC=0.6310593942670902, time=312.0
參數 numIterations=5, stepSize=200, miniBatchFraction=1.0, AUC=0.6347203760860913, time=248.0
參數 numIterations=15, stepSize=10, miniBatchFraction=0.5, AUC=0.6658330892678788, time=427.0
參數 numIterations=15, stepSize=10, miniBatchFraction=0.8, AUC=0.6643123737430628, time=457.0
參數 numIterations=15, stepSize=10, miniBatchFraction=1.0, AUC=0.6654726233657001, time=405.0
參數 numIterations=15, stepSize=50, miniBatchFraction=0.5, AUC=0.5765389265625821, time=449.0
參數 numIterations=15, stepSize=50, miniBatchFraction=0.8, AUC=0.5751383663384925, time=428.0
參數 numIterations=15, stepSize=50, miniBatchFraction=1.0, AUC=0.5750782886881294, time=405.0
參數 numIterations=15, stepSize=100, miniBatchFraction=0.5, AUC=0.5792799693603983, time=418.0
參數 numIterations=15, stepSize=100, miniBatchFraction=0.8, AUC=0.5764187712618559, time=404.0
參數 numIterations=15, stepSize=100, miniBatchFraction=1.0, AUC=0.5765389265625821, time=451.0
參數 numIterations=15, stepSize=200, miniBatchFraction=0.5, AUC=0.5793400470107614, time=371.0
參數 numIterations=15, stepSize=200, miniBatchFraction=0.8, AUC=0.5763586936114928, time=393.0
參數 numIterations=15, stepSize=200, miniBatchFraction=1.0, AUC=0.5792198917100352, time=408.0
參數 numIterations=20, stepSize=10, miniBatchFraction=0.5, AUC=0.6845397676496874, time=573.0
參數 numIterations=20, stepSize=10, miniBatchFraction=0.8, AUC=0.6779575100817807, time=503.0
參數 numIterations=20, stepSize=10, miniBatchFraction=1.0, AUC=0.6888015259723193, time=469.0
參數 numIterations=20, stepSize=50, miniBatchFraction=0.5, AUC=0.6517073317262562, time=477.0
參數 numIterations=20, stepSize=50, miniBatchFraction=0.8, AUC=0.6547487627758878, time=467.0
參數 numIterations=20, stepSize=50, miniBatchFraction=1.0, AUC=0.6504870044532558, time=558.0
參數 numIterations=20, stepSize=100, miniBatchFraction=0.5, AUC=0.6571894173218885, time=539.0
參數 numIterations=20, stepSize=100, miniBatchFraction=0.8, AUC=0.6588302881474306, time=406.0
參數 numIterations=20, stepSize=100, miniBatchFraction=1.0, AUC=0.6492065995298923, time=440.0
參數 numIterations=20, stepSize=200, miniBatchFraction=0.5, AUC=0.6612709426934312, time=381.0
參數 numIterations=20, stepSize=200, miniBatchFraction=0.8, AUC=0.6560291676992512, time=401.0
參數 numIterations=20, stepSize=200, miniBatchFraction=1.0, AUC=0.6505470821036189, time=419.0
參數 numIterations=60, stepSize=10, miniBatchFraction=0.5, AUC=0.6844796899993242, time=1192.0
參數 numIterations=60, stepSize=10, miniBatchFraction=0.8, AUC=0.68862129302123, time=1282.0
參數 numIterations=60, stepSize=10, miniBatchFraction=1.0, AUC=0.6858201725730507, time=1206.0
參數 numIterations=60, stepSize=50, miniBatchFraction=0.5, AUC=0.6439047468853493, time=1173.0
參數 numIterations=60, stepSize=50, miniBatchFraction=0.8, AUC=0.6410435487868069, time=1270.0
參數 numIterations=60, stepSize=50, miniBatchFraction=1.0, AUC=0.6453053071094389, time=1011.0
參數 numIterations=60, stepSize=100, miniBatchFraction=0.5, AUC=0.6504870044532558, time=914.0
參數 numIterations=60, stepSize=100, miniBatchFraction=0.8, AUC=0.6504870044532558, time=936.0
參數 numIterations=60, stepSize=100, miniBatchFraction=1.0, AUC=0.6560291676992512, time=1081.0
參數 numIterations=60, stepSize=200, miniBatchFraction=0.5, AUC=0.6490263665788031, time=938.0
參數 numIterations=60, stepSize=200, miniBatchFraction=0.8, AUC=0.6478060393058027, time=1037.0
參數 numIterations=60, stepSize=200, miniBatchFraction=1.0, AUC=0.6546286074751616, time=1123.0
參數 numIterations=100, stepSize=10, miniBatchFraction=0.5, AUC=0.6870404998460511, time=1446.0
參數 numIterations=100, stepSize=10, miniBatchFraction=0.8, AUC=0.688561215370867, time=1437.0
參數 numIterations=100, stepSize=10, miniBatchFraction=1.0, AUC=0.6858201725730507, time=1562.0
參數 numIterations=100, stepSize=50, miniBatchFraction=0.5, AUC=0.6249577579020884, time=1348.0
參數 numIterations=100, stepSize=50, miniBatchFraction=0.8, AUC=0.6180751120823664, time=1901.0
參數 numIterations=100, stepSize=50, miniBatchFraction=1.0, AUC=0.6233769647269095, time=1518.0
參數 numIterations=100, stepSize=100, miniBatchFraction=0.5, AUC=0.6509075480057975, time=1276.0
參數 numIterations=100, stepSize=100, miniBatchFraction=0.8, AUC=0.6504269268028927, time=1404.0
參數 numIterations=100, stepSize=100, miniBatchFraction=1.0, AUC=0.653228047251072, time=1354.0
參數 numIterations=100, stepSize=200, miniBatchFraction=0.5, AUC=0.6604110813226096, time=1421.0
參數 numIterations=100, stepSize=200, miniBatchFraction=0.8, AUC=0.6518875646773454, time=1337.0
參數 numIterations=100, stepSize=200, miniBatchFraction=1.0, AUC=0.653228047251072, time=1342.0
最佳參數 numIterations=20, stepSize=10, miniBatchFraction=1.0, AUC=0.6888015259723193
====== 測試模型 ======
測試最佳模型，結果 AUC=0.6536332528180353
====== 預測資料 ======
共計 3171 筆
網址 http://www.lynnskitchenadventures.com/2009/04/homemade-enchilada-sauce.html ==> 預測: 暫時性網頁
網址 http://lolpics.se/18552-stun-grenade-ar ==> 預測: 暫時性網頁
網址 http://www.xcelerationfitness.com/treadmills.html ==> 預測: 暫時性網頁
網址 http://www.bloomberg.com/news/2012-02-06/syria-s-assad-deploys-tactics-of-father-to-crush-revolt-threatening-reign.html ==> 預測: 暫時性網頁
網址 http://www.wired.com/gadgetlab/2011/12/stem-turns-lemons-and-limes-into-juicy-atomizers/ ==> 預測: 暫時性網頁
網址 http://www.latimes.com/health/boostershots/la-heb-fat-tax-denmark-20111013,0,2603132.story ==> 預測: 暫時性網頁
網址 http://www.howlifeworks.com/a/a?AG_ID=1186&cid=7340ci ==> 預測: 暫時性網頁
網址 http://romancingthestoveblog.wordpress.com/2010/01/13/sweet-potato-ravioli-with-lemon-sage-brown-butter-sauce/ ==> 預測: 暫時性網頁
網址 http://www.funniez.net/Funny-Pictures/turn-men-down.html ==> 預測: 暫時性網頁
網址 http://youfellasleepwatchingadvd.com/ ==> 預測: 暫時性網頁
===== 完成 ======
```
