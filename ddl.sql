CREATE TABLE iris (
    id INTEGER NOT NULL,
    sepal_length FLOAT NOT NULL,
    sepal_width FLOAT NOT NULL,
    petal_length FLOAT NOT NULL,
    petal_width FLOAT NOT NULL,
    class VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

LOAD CLASSES VoltMachineLearning.jar;
CREATE PROCEDURE FROM CLASS IrisNBClassifier;
CREATE PROCEDURE FROM CLASS IrisKNNClassifier;
CREATE FUNCTION nb_classify FROM METHOD IrisNBClassifier.classify;
CREATE FUNCTION knn_classify FROM METHOD IrisKNNClassifier.classify;

SELECT nb_classify(sepal_length, sepal_width, petal_length, petal_width) FROM iris;
SELECT knn_classify(sepal_length, sepal_width, petal_length, petal_width) FROM iris;

exec IrisNBClassifier;
exec IrisKNNClassifier;

SELECT sepal_length,
       sepal_width,
       petal_length,
       petal_width,
       class AS real_class,
       nb_classify(sepal_length, sepal_width, petal_length, petal_width) AS prediction
FROM iris;

SELECT sepal_length,
       sepal_width,
       petal_length,
       petal_width,
       class AS real_class,
       knn_classify(sepal_length, sepal_width, petal_length, petal_width) AS prediction
FROM iris;

SELECT SUM(CASE WHEN nb_classify(sepal_length,
                                 sepal_width,
                                 petal_length,
                                 petal_width) = class THEN 1 ELSE 0 END) AS nb_correct,
       SUM(CASE WHEN knn_classify(sepal_length,
                                  sepal_width,
                                  petal_length,
                                  petal_width) = class THEN 1 ELSE 0 END) AS knn_correct,
       COUNT(*) AS total
FROM iris;
