import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.configuration.Algo
import org.apache.spark.mllib.tree.impurity.{ Impurity, Entropy, Gini }
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.optimization.{ Updater, SimpleUpdater, L1Updater, SquaredL2Updater }
import org.apache.spark.mllib.classification.ClassificationModel

object classificationApp {
  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("classificationApp").setMaster("local[4]"))
    val numIterations = 10
    val maxTreeDepth = 5

    // Extracting data
    val rawData = sc.textFile("../../../data/train_noheader.tsv")
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
    val lrAccuracy = lrTotalCorrect / numData 
    println(s"lrAccuracy=${lrAccuracy}")

    val svmTotalCorrect = data.map{ lp => if (svmModel.predict(lp.features) == lp.label) 1 else 0 }.sum
    val svmAccuracy = svmTotalCorrect / numData
    println(s"svmAccuracy=${svmAccuracy}")

    val nbTotalCorrect = data.map{ lp => if (nbModel.predict(lp.features) == lp.label) 1 else 0 }.sum
    val nbAccuracy = nbTotalCorrect / numData
    println(s"nbAccuracy=${nbAccuracy}")

    val dtTotalCorrect = data.map{ lp =>
      val score = dtModel.predict(lp.features)
      val predicted = if (score > 0.5) 1 else 0
      if (predicted == lp.label) 1 else 0
    }.sum
    val dtAccuracy = dtTotalCorrect / numData
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

    // Improving model performance and tuning parameters
    //-- Feature standardization
    val vectors = data.map(lp => lp.features)
    val matrix = new RowMatrix(vectors)
    val matrixSummary = matrix.computeColumnSummaryStatistics()
    println(s"mean=${matrixSummary.mean}")
    println(s"min=${matrixSummary.min}")
    println(s"max=${matrixSummary.max}")
    println(s"variance=${matrixSummary.variance}")
    println(s"numNonzeros=${matrixSummary.numNonzeros}")

    val scaler = new StandardScaler(withMean=true, withStd=true).fit(vectors)
    val scaledData = data.map{ lp => LabeledPoint(lp.label, scaler.transform(lp.features)) }
    println(s"scaledData=${scaledData.first.features(0)}")
    println(s"scaledData=${(data.first.features(0) - matrixSummary.mean(0)) / math.sqrt(matrixSummary.variance(0))}")

    val lrModelScaled = LogisticRegressionWithSGD.train(scaledData, numIterations)
    val lrTotalCorrectScaled = scaledData.map{ lp => if (lrModelScaled.predict(lp.features) == lp.label) 1 else 0 }.sum
    val lrAccuracyScaled = lrTotalCorrectScaled / numData
    val lrScoredAndLabels = scaledData.map{ lp => (lrModelScaled.predict(lp.features), lp.label) }
    val lrMetricsScaled = new BinaryClassificationMetrics(lrScoredAndLabels)
    val lrPr = lrMetricsScaled.areaUnderPR
    val lrRoc = lrMetricsScaled.areaUnderROC
    println(f"${lrModelScaled.getClass.getSimpleName}\n\tAccuracy: ${lrAccuracyScaled * 100}%2.4f%%\n\tarea under PR: ${lrPr * 100}%2.4f%%\n\tarea under ROC: ${lrRoc * 100}%2.4f%%")

    //-- Category feature
    val categories = records.map(r => r(3)).distinct.collect.zipWithIndex.toMap
    val numCategories = categories.size
    println(s"numCategories: ${numCategories}")
    println("categories: " + categories)

    val dataCategories = records.map{ r =>
      val trimmed = r.map(_.replaceAll("\"", ""))
      val label = trimmed(r.size - 1).toInt
      val categoriesIdx = categories(r(3))
      val categoriesFeatures = Array.ofDim[Double](numCategories)
      categoriesFeatures(categoriesIdx) = 1.0
      val otherFeatures = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
      val features = categoriesFeatures ++ otherFeatures
      LabeledPoint(label, Vectors.dense(features))
    }
    println(s"dataCategories features: ${dataCategories.first.features}")

    val scaledCats = new StandardScaler(withMean=true, withStd=true).fit(dataCategories.map(lp => lp.features))
    val scaledDataCats = dataCategories.map{ lp => LabeledPoint(lp.label, scaledCats.transform(lp.features)) }
    println(s"scaledDataCats features: ${scaledDataCats.first.features}")

    val lrModelScaledCats = LogisticRegressionWithSGD.train(scaledDataCats, numIterations)
    val lrTotalCorrectScaledCats = scaledDataCats.map{ lp => if (lrModelScaledCats.predict(lp.features) == lp.label) 1 else 0 }.sum
    val lrAccuracyScaledCats = lrTotalCorrectScaledCats / numData
    val lrScoredAndLabelsCats = scaledDataCats.map{ lp => (lrModelScaledCats.predict(lp.features), lp.label) }
    val lrMetricsScaledCats = new BinaryClassificationMetrics(lrScoredAndLabelsCats)
    val lrPrCats = lrMetricsScaledCats.areaUnderPR
    val lrRocCats = lrMetricsScaledCats.areaUnderROC
    println(f"${lrModelScaledCats.getClass.getSimpleName}\n\tAccuracy: ${lrAccuracyScaledCats * 100}%2.4f%%\n\tarea under PR: ${lrPrCats * 100}%2.4f%%\n\tarea under ROC: ${lrRocCats * 100}%2.4f%%")

    //-- Correct form
    val dataNB = records.map{ r =>
      val trimmed = r.map(_.replaceAll("\"", ""))
      val label = trimmed(r.size - 1).toInt
      val categoryIdx = categories(r(3))
      val categoryFeatures = Array.ofDim[Double](numCategories)
      val otherFeatures = trimmed.slice(4, r.size - 1).map(d => if (d == "?") 0.0 else d.toDouble)
      categoryFeatures(categoryIdx) = 1.0
      LabeledPoint(label, Vectors.dense(categoryFeatures))
    }

    val nbModelCats = NaiveBayes.train(dataNB)
    val nbTotalCorrectCats = dataNB.map{ lp => if (nbModelCats.predict(lp.features) == lp.label) 1 else 0 }.sum
    val nbAccuracyCats = nbTotalCorrect / numData
    val nbScoredAndLabelsCats = dataNB.map{ lp => (nbModelCats.predict(lp.features), lp.label) }
    val nbMetricsCats = new BinaryClassificationMetrics(nbScoredAndLabelsCats)
    val nbPrCats = nbMetricsCats.areaUnderPR
    val nbRocCats = nbMetricsCats.areaUnderROC
    println(f"${nbModelCats.getClass.getSimpleName}\n\tAccuracy: ${nbAccuracyCats * 100}%2.4f%%\n\tarea under PR: ${nbPrCats * 100}%2.4f%%\n\tarea under ROC: ${nbRocCats * 100}%2.4f%%")

    //-- Tuning model parameters: Linear models
    def trainWithParams(input: RDD[LabeledPoint], regParam: Double, numIterations: Int, updater: Updater, stepSize: Double) = {
      val lr = new LogisticRegressionWithSGD
      lr.optimizer.setNumIterations(numIterations).setUpdater(updater).setRegParam(regParam).setStepSize(stepSize)
      lr.run(input)
    }
    def createMetrics(label: String, data: RDD[LabeledPoint], model: ClassificationModel) = {
      val scoredAndLabels = data.map{ lp => (model.predict(lp.features), lp.label) }
      val metrics = new BinaryClassificationMetrics(scoredAndLabels)
      (label, metrics.areaUnderROC)
    }
    scaledDataCats.cache

    //---- Iterations
    val iterResults = Seq(1, 5, 10, 50).map{ param =>
      val model = trainWithParams(scaledDataCats, 0.0, param, new SimpleUpdater, 1.0)
      createMetrics(s"$param iterations", scaledDataCats, model)
    }
    iterResults.foreach{ case (param, auc) => println(f"$param%16s, AUC = ${auc * 100}%2.2f%%") }

    //---- Step size
    val stepResults = Seq(0.01, 0.1, 1.0, 10.0).map{ param =>
      val model = trainWithParams(scaledDataCats, 0.0, numIterations, new SimpleUpdater, param)
      createMetrics(s"$param step size", scaledDataCats, model)
    }
    stepResults.foreach{ case (param, auc) => println(f"$param%16s, AUC = ${auc * 100}%2.2f%%") }

    //---- Regularization
    val regResults = Seq(0.001, 0.01, 0.1, 1.0, 10.0).map{ param =>
      val model = trainWithParams(scaledDataCats, param, numIterations, new SquaredL2Updater, 1.0)
      createMetrics(s"$param L2 Regularization parameter", scaledDataCats, model)
    }
    regResults.foreach{ case (param, auc) => println(f"$param%34s, AUC = ${auc * 100}%2.2f%%") }

    //-- Tuning model parameters: Decision trees
    def trainDTWithParams(input: RDD[LabeledPoint], maxDepth: Int, impurity: Impurity) = {
      DecisionTree.train(input, Algo.Classification, impurity, maxDepth)
    }

    //---- Entropy with different depth
    val dtResultsEntropy = Seq(1, 2, 3, 4, 5, 10, 20).map{ param =>
      val model = trainDTWithParams(data, param, Entropy)
      val scoredAndLabels = data.map{ lp =>
        val score = model.predict(lp.features)
        (if (score > 0.5) 1.0 else 0.0, lp.label)
      }
      val metrics = new BinaryClassificationMetrics(scoredAndLabels)
      (s"$param tree depth", metrics.areaUnderROC)
    }
    dtResultsEntropy.foreach{ case (param, auc) => println(f"$param%16s, AUC = ${auc * 100}%2.2f%%") }

    //---- Gini with different depth
    val dtResultsGini = Seq(1, 2, 3, 4, 5, 10, 20).map{ param =>
      val model = trainDTWithParams(data, param, Gini)
      val scoredAndLabels = data.map{ lp =>
        val score = model.predict(lp.features)
        (if (score > 0.5) 1.0 else 0.0, lp.label)
      }
      val metrics = new BinaryClassificationMetrics(scoredAndLabels)
      (s"$param tree depth", metrics.areaUnderROC)
    }
    dtResultsGini.foreach{ case (param, auc) => println(f"$param%16s, AUC = ${auc * 100}%2.2f%%") }

    //-- Tuning model parameters: Naive Bayes models
    def trainNBWithParams(input: RDD[LabeledPoint], lambda: Double) = {
      val nb = new NaiveBayes
      nb.setLambda(lambda)
      nb.run(input)
    }
    val nbResults = Seq(0.001, 0.01, 0.1, 1.0, 10.0).map{ param =>
      val model = trainNBWithParams(dataNB, param)
      val scoredAndLabels = dataNB.map{ lp => (model.predict(lp.features), lp.label) }
      val metrics = new BinaryClassificationMetrics(scoredAndLabels)
      (s"$param lambda", metrics.areaUnderROC)
    }
    nbResults.foreach{ case (param, auc) => println(f"$param%16s, AUC = ${auc * 100}%2.2f%%") }

    //-- Cross-validation
    val Array(train, test) = scaledDataCats.randomSplit(Array(0.6, 0.4), 123)

    val regResultsTest = Seq(0.0, 0.001, 0.0025, 0.005, 0.01).map{ param =>
      val model = trainWithParams(train, param, numIterations, new SquaredL2Updater, 1.0)
      createMetrics(s"$param L2 regularization parameter", test, model)
    }
    regResultsTest.foreach{ case (param, auc) => println(f"$param%34s, AUC = ${auc * 100}%2.6f%%") }

    val regResultsTrain = Seq(0.0, 0.001, 0.0025, 0.005, 0.01).map{ param =>
      val model = trainWithParams(train, param, numIterations, new SquaredL2Updater, 1.0)
      createMetrics(s"$param L2 regularization parameter", train, model)
    }
    regResultsTrain.foreach{ case (param, auc) => println(f"$param%34s, AUC = ${auc * 100}%2.6f%%") }
  }
}
