import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;

public class IrisKNNClassifier extends VoltProcedure {

    // The query to extract the data from a VoltDB table.
    public final SQLStmt selectData =
            new SQLStmt("SELECT sepal_length, sepal_width, petal_length, petal_width, class FROM iris;");

    // Number of attributes.
    private static final int attributeCount = 4;

    private static Classifier knnc;

    // The train method (Java stored procedure).
    public long run() {
        // Run the SELECT query to get the data.
        voltQueueSQL(selectData);
        VoltTable[] queryResults = voltExecuteSQL(true);
        if (queryResults.length == 0) {
            throw new VoltAbortException("No data is returned.");
        }
        VoltTable dataTable = queryResults[0];

        // Build the data set that can be used by the machine learning library.
        Dataset dataset = new DefaultDataset();
        while (dataTable.advanceRow()) {
            DenseInstance dataRow = new DenseInstance(attributeCount);
            for (int i = 0; i < attributeCount; i++) {
                dataRow.put(i, dataTable.getDouble(i));
            }
            dataRow.setClassValue(dataTable.getString(attributeCount));
            dataset.add(dataRow);
        }

        // Train classifier.
        knnc = new KNearestNeighbors(5);
        knnc.buildClassifier(dataset);

        return ClientResponse.SUCCESS;
    }

    public String classify(double sepal_length, Double sepal_width, double petal_length, Double petal_width) {
        if (knnc == null) {
            throw new RuntimeException("Please run the IrisKNNClassifier procedure first to train the model.");
        }
        // Assemble the attributes together.
        DenseInstance dataRow = new DenseInstance(attributeCount);
        dataRow.put(0, sepal_length);
        dataRow.put(1, sepal_width);
        dataRow.put(2, petal_length);
        dataRow.put(3, petal_width);
        Object predictedClassValue = knnc.classify(dataRow);
        return (String) predictedClassValue;
    }
}
