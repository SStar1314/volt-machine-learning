-- Train the two models on the iris dataset.
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
