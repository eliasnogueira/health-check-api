package healthcheck.support;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the health_check.json and assert if is valid against schema.json on resources
 *
 * @author Elias Nogueira, Gregory Severo
 */
public class ReadJSON {

    //private static FileReader healthCheck;

    private static String healthCheck;

    /**
     * Read the json file and transform it on an array of object
     * @return and array of objects that contains name and url
     * @throws Exception
     */
    public static List<Object[]> read() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();
        String fileFromMavenProperty = System.getProperty("file");

        if (validaSchemaJSON()) {
            JSONParser parser = new JSONParser();
            //Object file = parser.parse(healthCheckFile(fileFromMavenProperty));
            Object file = parser.parse(new FileReader(healthCheckFile(fileFromMavenProperty)));
            JSONObject jsonObject = (JSONObject) file;
            JSONArray tests = (JSONArray) jsonObject.get("test");

            for (int i = 0; i < tests.size(); i++) {
                String[] informations = new String[2];
                JSONObject obj = (JSONObject) tests.get(i);
                informations[0] = obj.get("name").toString();
                informations[1] = obj.get("url").toString();
                data.add(informations);
            }
        }

        return data;
    }

    /**
     * Assert that health_check.json in valid
     * @return <i>true</i> if the file is valid or <i>false</i> if not
     * @throws Exception
     */
    private static boolean validaSchemaJSON() throws Exception {
        JsonSchemaFactory jsonSchemaFactory;
        JsonSchema jsonSchema;
        ProcessingReport processingReport;
        boolean result;
         
        jsonSchemaFactory = JsonSchemaFactory.byDefault();
        jsonSchema = jsonSchemaFactory.getJsonSchema(JsonLoader.fromResource("/schema.json"));
        processingReport = jsonSchema.validate(JsonLoader.fromPath("health_check.json"));

        result = processingReport.isSuccess();
         
        // an error occurs in json schema validation
        if (!result) {
           throw new Exception(processingReport.toString());
        }
         
        return result;
    }

    /**
     * Return the proper health_check.json. If the parameter <i>-Dfile=filename.json</i> is not set on command line
     * the file health_check.json will be used. Otherwise the file informed will be used.
     * @param file health_check file
     * @return the proper health_check file
     * @throws Exception
     */
    private static String healthCheckFile(String file) throws Exception {

        if (file == null) {
            healthCheck = "health_check.json";
        } else {
            healthCheck = file;
        }

        return healthCheck;
    }

    public static String getFileName() {
        return healthCheck.replace(".json", "");
    }

    public static void createReportDir() {
        new File("target/report").mkdir();
    }
}
