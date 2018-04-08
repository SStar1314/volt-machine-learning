package demo

import org.shaded.jpmml.model.JAXBUtil
import org.shaded.jpmml.sparkml.ConverterUtil
import javax.xml.transform.stream.StreamResult
import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.ml.classification.BinaryLogisticRegressionSummary
import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.functions.max

/*
 * https://archive.ics.uci.edu/ml/datasets/Breast+Cancer+Wisconsin+(Original)
 * Array(1000025,5,1,1,1,2,1,3,1,1,2)
   0. Sample code number            id number 
   1. Clump Thickness               1 - 10
   2. Uniformity of Cell Size       1 - 10
   3. Uniformity of Cell Shape      1 - 10
   4. Marginal Adhesion             1 - 10
   5. Single Epithelial Cell Size   1 - 10
   6. Bare Nuclei                   1 - 10
   7. Bland Chromatin               1 - 10
   8. Normal Nucleoli               1 - 10
   9. Mitoses                       1 - 10
  10. Class:                        (2 for benign, 4 for malignant)
 */

case class Record(target: Double, thickness: Double, size: Double, shape: Double, madh: Double,
                  epsize: Double, bnuc: Double, bchrom: Double, nNuc: Double, mit: Double)

object Cancer {

  def parseRecord(line: Array[Double]): Record = {
    Record(
      if (line(9) == 4.0) 1 else 0, line(0), line(1), line(2), line(3), line(4), line(5), line(6), line(7), line(8)
    )
  }
  
  // function to transform an RDD of Strings into an RDD of Double, filter lines with ?, remove first column
  def parseRDD(rdd: RDD[String]): RDD[Array[Double]] = {
    rdd.map(_.split(",")).filter(_(6) != "?").map(_.drop(1)).map(_.map(_.toDouble))
  }

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("volt-pmml")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val fileRDD = sc.textFile("data/original.csv")
    val recordRDD = parseRDD(fileRDD).map(parseRecord)

    import sqlContext.implicits._
    val recordDF = recordRDD.toDF().cache()
    val splitSeed = 5043

    val featureCols = Array("thickness", "size", "shape", "madh", "epsize", "bnuc", "bchrom", "nNuc", "mit")
    val vectorAssembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    // val vectorDF = vectorAssembler.transform(recordDF)
    val Array(trainingData, testData) = recordDF.randomSplit(Array(0.7, 0.3), splitSeed)
    val labelIndexer = new StringIndexer().setInputCol("target").setOutputCol("label").fit(trainingData)
    // val finalDF = labelIndexer.fit(assembledDF).transform(assembledDF)
    val lr = new LogisticRegression().setMaxIter(10)
                                     .setRegParam(0.3)
                                     .setElasticNetParam(0.8)
                                     .setLabelCol("label")
                                     .setFeaturesCol("features")
    val pipeline = new Pipeline().setStages(Array(labelIndexer, vectorAssembler, lr))
    val pipeLineModel = pipeline.fit(trainingData)
    val pmml = ConverterUtil.toPMML(trainingData.schema, pipeLineModel);
    JAXBUtil.marshalPMML(pmml, new StreamResult(new File("model.pmml")))
    JAXBUtil.marshalPMML(pmml, new StreamResult(System.out))

    val predictions = pipeLineModel.transform(testData)
    val trainingSummary = pipeLineModel.stages.last.asInstanceOf[LogisticRegressionModel].summary
    val binarySummary = trainingSummary.asInstanceOf[BinaryLogisticRegressionSummary]
    val evaluator = new BinaryClassificationEvaluator().setLabelCol("label")
    val accuracy = evaluator.evaluate(predictions)
    println("accuracy: " + accuracy)
  }
}
