import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.opendatagroup.antinous.pfainterface.PFAEngineFactory;
import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;

public class Main {

    public static String readJSON(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded);
        } catch (IOException e) {
            return "";
        }
    }

    public static void main(String[] args) {
        PFAEngineFactory factory = new PFAEngineFactory();
        String modelJSON = readJSON("model.pfa");
        System.out.println("Model loaded from model.pfa:\n" + modelJSON);
        PFAEngine<Object, Object> engine = factory.engineFromJson(modelJSON);
        String testDataJSON = readJSON("test.json");
        System.out.println("Test data:\n" + testDataJSON);
        Object output = engine.action(engine.jsonInput(testDataJSON));
        System.out.println("Prediction: " + engine.jsonOutput(output));
    }

}
