
package demo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;

public class Scorer {

    public int score(int id, int thickness, int size, int shape, int madh, int epsize,
                     int bnuc, int bchrom, int nNuc, int mit, int target) throws Exception {
        Map<String, Integer> dataByCol = new HashMap<>();
        dataByCol.put("id", id);
        dataByCol.put("thickness", thickness);
        dataByCol.put("size", size);
        dataByCol.put("shape", shape);
        dataByCol.put("madh", madh);
        dataByCol.put("epsize", epsize);
        dataByCol.put("bnuc", bnuc);
        dataByCol.put("bchrom", bchrom);
        dataByCol.put("mit", mit);
        dataByCol.put("target", target == 4 ? 1 : 0);

        PMML pmml;
        try (InputStream is = new FileInputStream("model.pmml")) {
            pmml = PMMLUtil.unmarshal(is);
        }

        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        List<InputField> inputFields = evaluator.getInputFields();
        for (InputField inputField : inputFields){
            FieldName inputFieldName = inputField.getName();
            FieldValue inputFieldValue = inputField.prepare(dataByCol.get(inputFieldName.getValue()));
            arguments.put(inputFieldName, inputFieldValue);
        }
        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        FieldName outputFieldName = evaluator.getOutputFields().get(0).getName();
        Double prediction = (Double)results.get(outputFieldName);
        return prediction.intValue();
    }
}

