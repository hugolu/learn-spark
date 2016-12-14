# ML 小抄

## Recommendation System

### 訓練資料
```scala
import org.apache.spark.mllib.recommendation.Rating

case class Rating(user: Int, product: Int, rating: Double) extends Product with Serializable
```

### 進行訓練
```scala
import org.apache.spark.mllib.recommendation.ALS

object ALS extends Serializable
  def train(ratings: RDD[Rating], rank: Int, iterations: Int, lambda: Double): MatrixFactorizationModel
```

### 輸出模型
```scala
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

class MatrixFactorizationModel extends Saveable with Serializable with Logging
  val rank: Int
  val userFeatures: RDD[(Int, Array[Double])]
  val productFeatures: RDD[(Int, Array[Double])]
  def recommendProducts(user: Int, num: Int): Array[Rating]
  def recommendUsers(product: Int, num: Int): Array[Rating]
  def predict(user: Int, product: Int): Double
```

### 評估模型

#### MSE, MRSE (評量預測與真實的誤差-顯示評級)
```scala
import org.apache.spark.mllib.evaluation.RegressionMetrics

class RegressionMetrics extends Logging
  new RegressionMetrics(predictionAndObservations: RDD[(Double, Double)])
  def meanSquaredError: Double
  def rootMeanSquaredError: Double
```
```scala
val metrics = new RegressionMetrics(predictAndRatings)
metrics.meanSquaredError
metrics.rootMeanSquaredError
```

#### MAP (基於排名的評估指標-隱式評級)
```scala
import org.apache.spark.mllib.evaluation.RankingMetrics

class RankingMetrics[T] extends Logging with Serializable
  new RankingMetrics(predictionAndLabels: RDD[(Array[T], Array[T])])(implicit arg0: ClassTag[T])
  lazy val meanAveragePrecision: Double
```
```scala
val metrics = new RankingMetrics(predictAndRatings)
metrics.meanAveragePrecision
```
