## Apply machine learning models in VoltDB using UDFs powered by Java-ML

This directory contains a very simple example where we apply the Naive Bayes Classifier and the K-nearest neighbours algorithm on the [Iris](https://archive.ics.uci.edu/ml/datasets/iris) dataset in VoltDB using two UDFs powered by the [Java-ML](http://java-ml.sourceforge.net/) library.

#### How to compile:

Before you compile, please make sure that you have [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Ant](https://ant.apache.org/bindownload.cgi) installed on your computer.

To compile, simply run:
```bash
ant
```

After the compilation completes, you will see a JAR file named `volt-javaml.jar`. This file contains the compiled stored procedures and UDFs.

#### How to run:

1. This example depends on the Java-ML library, which can be located at `lib/javaml-0.1.7.jar`. In order to make VoltDB be aware of this library, you need to copy this JAR file to `${VOLTDB_INSTALLATION_PATH}/lib`.

2. Start a VoltDB server using the deployment file provided in this folder:
```
  voltdb init --force -C deployment.xml
  voltdb start &
```
This deployment file tells VoltDB to use just one site per host because in this simple example, the model is stored as a static member of the class where both the stored procedure and the UDF reside. **One should not expect this will be the case for a production system.**

3. Load the table schema, stored procedures, UDFs by running:
```
  sqlcmd < ddl.sql
```

4. Load the Iris dataset:
```
  sqlcmd < iris.sql
```

5. Train the model and score the data:
```
  sqlcmd < run.sql
```

VoltDB is not best for training the model. In this example, we used a stored procedure to get the data out  from the table and train the model on it. Then we use queires to call UDF on the data records to get a score. The last query in the `run.sql` file counts the number of accurate predictions for each model respectively so that we can compare how accurate those two models are.
