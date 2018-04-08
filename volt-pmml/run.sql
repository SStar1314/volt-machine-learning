SELECT COUNT(*) AS total, 
       SUM(CASE WHEN target/4 = score(id, thickness, size, shape, madh, epsize, bnuc, bchrom, nNuc, mit, target)
                THEN 1 ELSE 0 END) AS correct
FROM testSet;
