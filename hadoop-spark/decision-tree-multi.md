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
source: [Classification/src/main/scala/RunDecisionTreeBinary.scala](Classification/src/main/scala/RunDecisionTreeBinary.scala)

函數 | 說明
-----|-----
`main`          | 主程式，包含準備資料、訓練模型、測試模型、預測資料
`setLogger`     | 關閉 log & console 訊息
`prepareData`   | 匯入資料，建立 LabeledPoint、講資料分成 train, evaluation, test 三份
`trainEvaluateTunning` | 交差訓練各種參數組合的模型
`trainEvaluate` | 訓練評估流程，包含訓練模型、評估模型
`trainModel`    | 訓練模型
`evaluateModel` | 評估模型
`predictData`   | 使用模型預測資料

### prepareData
```scala
def PrepareData(sc: SparkContext): (RDD[LabeledPoint], RDD[LabeledPoint], RDD[LabeledPoint]) = {
  ...
  //-- 2. 建立 RDD[LabeledPoint]
  val labelpointRDD = rawData.map{ record =>
    val fields = record.split(',').map(_.toDouble)
    val label = fields.last - 1
    LabeledPoint(label, Vectors.dense(fields.init))
  }
  ...
}
```
- 原本 label 值範圍為 1~7，但要執行 DecisionTree.trainClassifier 訓練，label 從 0 開始，所以 `fields.last - 1`

### trainModel
```scala
def trainModel(trainData: RDD[LabeledPoint], impurity: String, maxDepth: Int, maxBins: Int): (DecisionTreeModel, Long) = {
  ...
  val model = DecisionTree.trainClassifier(trainData, 7, Map[Int, Int](), impurity, maxDepth, maxBins)
  ...
}
```
- `numclasses` 從原本的 2 (for DecisionTreeBinary) 變為 7 (label 有七種可能) 

### 調校最佳模型
```shell
$ sbt package
$ spark-submit --class RunDecisionTreeMulti --jars lib/joda-time-2.9.4.jar --driver-memory 1024M target/scala-2.11/classification_2.11-1.0.0.jar
====== 準備階段 ======
開始匯入資料
共計 581012 筆
資料分成 trainData: 464726, validationData: 58267, testData = 58019
====== 訓練評估 ======
impurity=   gini, maxDepth= 3, maxBins=  3 ==> AUC=0.66, time=12983ms
impurity=   gini, maxDepth= 3, maxBins=  5 ==> AUC=0.68, time=3855ms
impurity=   gini, maxDepth= 3, maxBins= 10 ==> AUC=0.68, time=3684ms
...
最佳參數 impurity=entropy, maxDepth=20, maxBins=50, AUC=0.91
====== 測試模型 ======
測試最佳模型，結果 AUC=0.914803771178407
====== 預測資料 ======
共計 581012 筆
土地條件 海拔:2617.0, 方位:45.0, 斜率:＄9.0, 水源垂直距離:240.0, 水源水平距離:56.0, 9點時陰影:666.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2495.0, 方位:51.0, 斜率:＄7.0, 水源垂直距離:42.0, 水源水平距離:2.0, 9點時陰影:752.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2880.0, 方位:209.0, 斜率:＄17.0, 水源垂直距離:216.0, 水源水平距離:30.0, 9點時陰影:4986.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2768.0, 方位:114.0, 斜率:＄23.0, 水源垂直距離:192.0, 水源水平距離:82.0, 9點時陰影:3339.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2962.0, 方位:148.0, 斜率:＄16.0, 水源垂直距離:323.0, 水源水平距離:23.0, 9點時陰影:5916.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2739.0, 方位:117.0, 斜率:＄24.0, 水源垂直距離:127.0, 水源水平距離:53.0, 9點時陰影:3281.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2515.0, 方位:41.0, 斜率:＄9.0, 水源垂直距離:162.0, 水源水平距離:4.0, 9點時陰影:680.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2502.0, 方位:81.0, 斜率:＄7.0, 水源垂直距離:175.0, 水源水平距離:11.0, 9點時陰影:912.0 => 預測:4.0, 實際:4.0, 結果:正確
土地條件 海拔:2791.0, 方位:63.0, 斜率:＄10.0, 水源垂直距離:418.0, 水源水平距離:48.0, 9點時陰影:2942.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2788.0, 方位:13.0, 斜率:＄16.0, 水源垂直距離:30.0, 水源水平距離:8.0, 9點時陰影:4126.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2562.0, 方位:354.0, 斜率:＄12.0, 水源垂直距離:67.0, 水源水平距離:9.0, 9點時陰影:1057.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2751.0, 方位:88.0, 斜率:＄5.0, 水源垂直距離:400.0, 水源水平距離:30.0, 9點時陰影:2322.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:3057.0, 方位:124.0, 斜率:＄12.0, 水源垂直距離:150.0, 水源水平距離:53.0, 9點時陰影:6508.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2991.0, 方位:49.0, 斜率:＄13.0, 水源垂直距離:85.0, 水源水平距離:0.0, 9點時陰影:6199.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2537.0, 方位:7.0, 斜率:＄12.0, 水源垂直距離:0.0, 水源水平距離:0.0, 9點時陰影:1583.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:3142.0, 方位:220.0, 斜率:＄11.0, 水源垂直距離:424.0, 水源水平距離:69.0, 9點時陰影:6216.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:3185.0, 方位:328.0, 斜率:＄16.0, 水源垂直距離:127.0, 水源水平距離:13.0, 9點時陰影:4763.0 => 預測:0.0, 實際:0.0, 結果:正確
土地條件 海拔:2860.0, 方位:276.0, 斜率:＄33.0, 水源垂直距離:60.0, 水源水平距離:33.0, 9點時陰影:5292.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2998.0, 方位:102.0, 斜率:＄12.0, 水源垂直距離:384.0, 水源水平距離:158.0, 9點時陰影:6026.0 => 預測:1.0, 實際:1.0, 結果:正確
土地條件 海拔:2622.0, 方位:95.0, 斜率:＄12.0, 水源垂直距離:484.0, 水源水平距離:39.0, 9點時陰影:1329.0 => 預測:1.0, 實際:1.0, 結果:正確
===== 完成 ======
```
- 最佳模型參數 impurity=entropy, maxDepth=20, maxBins=50, 訓練 AUC=0.91 與評估 AUC=0.914803771178407 相差不大，無 overfitting

====== 測試模型 ======
### 測試最佳模型
```shell
$ spark-submit --class RunDecisionTreeMulti --jars lib/joda-time-2.9.4.jar --driver-memory 1024M target/scala-2.11/classification_2.11-1.0.0.jar 
```
