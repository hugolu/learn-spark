import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.configuration.Algo
import org.apache.spark.mllib.tree.impurity.Entropy
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics

object classificationApp {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("classificationApp").setMaster("local[4]"))
    val numIterations = 10
    val maxTreeDepth = 5

    // Extracting data
    val rawData = sc.textFile("../data/train_noheader.tsv")
    val records = rawData.map(line => line.split("\t"))
    records.first().foreach(println)

    // Cleaning data, deal with missing values
    val data = records.map{ r =>
      val trimmed = r.map(_.replaceAll("\"", ""))
      val label = trimmed(r.size - 1).toInt
      val features = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
      LabeledPoint(label, Vectors.dense(features))
    }
    data.cache
    val numData = data.count
    println(s"numData=${numData}")

    val nbData = records.map{ r =>
      val trimmed = r.map(_.replaceAll("\"", ""))
      val label = trimmed(r.size - 1).toInt
      val features = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble).map(d => if (d < 0) 0.0 else d)
      LabeledPoint(label, Vectors.dense(features))
    }
    val numNbData = nbData.count
    println(s"numNbData=${numNbData}")

    // Training models
    val lrModel = LogisticRegressionWithSGD.train(data, numIterations)
    val svmModel = SVMWithSGD.train(data, numIterations)
    val nbModel = NaiveBayes.train(nbData)
    val dtModel = DecisionTree.train(data, Algo.Classification, Entropy, maxTreeDepth)

    // Using models
    val dataPoint = data.first
    val prediction = lrModel.predict(dataPoint.features)
    val trueLabel = dataPoint.label
    println(s"Logistic Model: prediction=${prediction}, trueLabel=${trueLabel}")

    val predictions = lrModel.predict(data.map(lp => lp.features))
    val trueLabels = data.map(lp => lp.label)
    predictions.zip(trueLabels).take(10).foreach(println)

    // Evaluating performance 
    //-- Accuracy
    val lrTotalCorrect = data.map{ lp => if (lrModel.predict(lp.features) == lp.label) 1 else 0 }.sum
    val lrAccuracy = lrTotalCorrect / data.count 
    println(s"lrAccuracy=${lrAccuracy}")

    val svmTotalCorrect = data.map{ lp => if (svmModel.predict(lp.features) == lp.label) 1 else 0 }.sum
    val svmAccuracy = svmTotalCorrect / data.count
    println(s"svmAccuracy=${svmAccuracy}")

    val nbTotalCorrect = data.map{ lp => if (nbModel.predict(lp.features) == lp.label) 1 else 0 }.sum
    val nbAccuracy = nbTotalCorrect / data.count
    println(s"nbAccuracy=${nbAccuracy}")

    val dtTotalCorrect = data.map{ lp =>
      val score = dtModel.predict(lp.features)
      val predicted = if (score > 0.5) 1 else 0
      if (predicted == lp.label) 1 else 0
    }.sum
    val dtAccuracy = dtTotalCorrect / data.count
    println(s"dtAccuracy=${dtAccuracy}")

    //-- Area under PR (Precision & Recall), Area under ROC(Receiver Operating Characteristic curve)
    val metrics = Seq(lrModel, svmModel).map{ model =>
      val scoreAndLabels = data.map{ lp => (model.predict(lp.features), lp.label) }
      val metrics = new BinaryClassificationMetrics(scoreAndLabels)
      (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
    }

    val nbMetrics = Seq(nbModel).map{ model =>
      val scoreAndLabels = data.map{ lp =>
        val score = model.predict(lp.features)
        (if (score > 0.5) 1.0 else 0.0, lp.label)
      }
      val metrics = new BinaryClassificationMetrics(scoreAndLabels)
      (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
    }

    val dtMetrics = Seq(dtModel).map{ model =>
      val scoreAndLabels = data.map{ lp =>
        val score = model.predict(lp.features)
        (if (score > 0.5) 1.0 else 0.0, lp.label)
      }
      val metrics = new BinaryClassificationMetrics(scoreAndLabels)
      (model.getClass.getSimpleName, metrics.areaUnderPR, metrics.areaUnderROC)
    }

    val allMetrics = metrics ++ nbMetrics ++ dtMetrics
    allMetrics.foreach{ case (m, pr, roc) =>
      println(f"$m, Area under PR: ${pr * 100.0}%2.4f%%, Area under ROC: ${roc * 100.0}%2.4f%%")
    }
  }
}
