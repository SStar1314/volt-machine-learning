CREATE TABLE testSet (
    id INTEGER NOT NULL,
    thickness INTEGER NOT NULL,
    size INTEGER NOT NULL,
    shape INTEGER NOT NULL,
    madh INTEGER NOT NULL,
    epsize INTEGER NOT NULL,
    bnuc INTEGER NOT NULL,
    bchrom INTEGER NOT NULL,
    nNuc INTEGER NOT NULL,
    mit INTEGER NOT NULL,
    target INTEGER NOT NULL
);
PARTITION TABLE testSet ON COLUMN id;

LOAD CLASSES 'Scorer/target/pmml-udf-1.0.jar';
CREATE FUNCTION score FROM METHOD demo.Scorer.score;