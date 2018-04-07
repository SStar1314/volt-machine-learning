CREATE TABLE iris (
    id INTEGER NOT NULL,
    sepal_length FLOAT NOT NULL,
    sepal_width FLOAT NOT NULL,
    petal_length FLOAT NOT NULL,
    petal_width FLOAT NOT NULL,
    class VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

LOAD CLASSES volt-javaml.jar;
CREATE PROCEDURE FROM CLASS IrisNBClassifier;
CREATE PROCEDURE FROM CLASS IrisKNNClassifier;
CREATE FUNCTION nb_classify FROM METHOD IrisNBClassifier.classify;
CREATE FUNCTION knn_classify FROM METHOD IrisKNNClassifier.classify;
