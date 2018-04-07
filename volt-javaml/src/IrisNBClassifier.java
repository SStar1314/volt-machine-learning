import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.filter.discretize.EqualWidthBinning;

public class IrisNBClassifier extends VoltProcedure {

    // The query to extract the data from a VoltDB table.
    public final SQLStmt selectData =
            new SQLStmt("SELECT sepal_length, sepal_width, petal_length, petal_width, class FROM iris;");

    // Number of attributes.
    private static final int attributeCount = 4;

    private static Classifier nbc;
    private static EqualWidthBinning eb;

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

        // Discretize through EqualWidtBinning
        eb = new EqualWidthBinning(20);
        eb.build(dataset);
        Dataset ddata = dataset.copy();
        eb.filter(ddata);

        // Train classifier.
        boolean useLaplace = true;
        boolean useLogs = true;
        boolean sparse = false;
        nbc = new NaiveBayesClassifier(useLaplace, useLogs, sparse);
        nbc.buildClassifier(dataset);

        return ClientResponse.SUCCESS;
    }

    public String classify(double sepal_length, Double sepal_width, double petal_length, Double petal_width) {
        if (eb == null || nbc == null) {
            throw new RuntimeException("Please run the IrisNBClassifier procedure first to train the model.");
        }
        // Assemble the attributes together.
        DenseInstance dataRow = new DenseInstance(attributeCount);
        dataRow.put(0, sepal_length);
        dataRow.put(1, sepal_width);
        dataRow.put(2, petal_length);
        dataRow.put(3, petal_width);
        eb.filter(dataRow);
        Object predictedClassValue = nbc.classify(dataRow);
        return (String) predictedClassValue;
    }
}
