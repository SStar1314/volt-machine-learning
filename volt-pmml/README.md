## VoltDB integration with PMML

In this directory, we provide an example app showing how VoltDB can support the PMML model exported from Spark and score the dataset with that using a UDF.

#### How to compile the Spark program:

Before you compile, please make sure that you have [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Maven](http://maven.apache.org/download.cgi) installed on your computer.

To compile, simply run:
```
mvn install
```

#### How to compile the VoltDB UDF:

```
cd Scorer
mvn install
```

#### Run the Spark program to train and export the model:

You need to have Spark installed on your computer to run the Spark part.

In the `volt-pmml` folder, run:

```
spark-submit --jars lib/jpmml-sparkml-1.4-SNAPSHOT.jar,lib/pmml-model-1.4-SNAPSHOT.jar \
             --class=demo.Cancer target/volt-pmml-1.0.jar \
             --master=local[2] \
             --deploy-mode cluster
```

This program will read the data stored in `data/` folder and train a logistic regression model on it. The model will be exported to the current folder as `model.pmml`.

This `model.pmml` file is also included in the repository.

#### Use VoltDB UDF to parse the PMML model and score the data:

1. Copy all the jar files in the `Scorer/lib` folder to `${VOLTDB_INSTALLATION_PATH}/lib`.
2. Start a VoltDB server:
```
  voltdb init --force
  voltdb start &
```

3. Define the table and the UDF:
```
  sqlcmd < ddl.sql
```

4. Load the test data:
```
  csvloader testSet -f data/test.csv
```
You should see the following result:
```
Read 196 rows from file and successfully inserted 196 rows (final)
Elapsed time: 0.319 seconds
Invalid row file: /Users/yzhang/Github/volt-machine-learning/volt-pmml/csvloader_TESTSET_insert_invalidrows.csv
Log file: /Users/yzhang/Github/volt-machine-learning/volt-pmml/csvloader_TESTSET_insert_log.log
Report file: /Users/yzhang/Github/volt-machine-learning/volt-pmml/csvloader_TESTSET_insert_report.log
```

5. Suppose you already have the `model.pmml` file in the current folder, run the query:

```
sqlcmd < run.sql
```
You will see the result as follows:
```sql
SELECT COUNT(*) AS total, 
       SUM(CASE WHEN target/4 = score(id, thickness, size, shape, madh, epsize, bnuc, bchrom, nNuc, mit, target)
                THEN 1 ELSE 0 END) AS correct
FROM testSet;

TOTAL  CORRECT 
------ --------
   196      170

(Returned 1 rows in 1.08s)
```
