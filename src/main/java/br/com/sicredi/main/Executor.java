package br.com.sicredi.main;


import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Executor {

    private static JsonSchemaFactory jsonSchemaFactory;
    private static JsonSchema jsonSchema;
    private static ProcessingReport processingReport;
    private static JSONParser parser = new JSONParser();

    private static ExtentReports extent = null;
    private static ExtentHtmlReporter htmlReporter;
    private static ExtentTest test;


    public static void main(String[] args) {
        extent = new ExtentReports();
        htmlReporter = new ExtentHtmlReporter("resultado.html");
        htmlReporter.config().setDocumentTitle("Health Check - Serviços");
        extent.attachReporter(htmlReporter);

        try {
            if (validaSchemaJSON()) {
                executaTesteHealthCheck();
            }

        } catch (Exception e) {
            test = extent.createTest("Falha na execução de todos os testes");
            test.fail(e.getMessage());
            e.printStackTrace();
        } finally {
            extent.flush();
        }
    }

    /**
     * Verifica se o schema (formato do arquivo json) é válido
     * @return <i>true</i> para válido e <i>false</i> para inválido
     * @throws Exception
     */
    private static boolean validaSchemaJSON() throws Exception {
        boolean resultado = true;

        jsonSchemaFactory = JsonSchemaFactory.byDefault();
        jsonSchema = jsonSchemaFactory.getJsonSchema(JsonLoader.fromResource("/schema.json"));
        processingReport = jsonSchema.validate(JsonLoader.fromPath("health_check.json"));

        resultado = processingReport.isSuccess();

        // ocorreu uma falha na validacao do schema
        if (!resultado) {
            throw new Exception(processingReport.toString());
        }

        return resultado;
    }

    /**
     * Execute o teste de health check nos serviços
     * @throws Exception
     */
    private static void executaTesteHealthCheck() throws Exception {
        HttpURLConnection http;
        int statusCode = 0;

        Object arquivo = parser.parse(new FileReader("health_check.json"));
        JSONObject jsonObject = (JSONObject) arquivo;

        JSONArray testes = (JSONArray) jsonObject.get("testes");

        for (int i = 0; i < testes.size(); i++) {
            JSONObject obj = (JSONObject) testes.get(i);

            test = extent.createTest(obj.get("nome").toString());

            // passa para o HTTPURLConnection o endpoint do serviço
            http = (HttpURLConnection) new URL(obj.get("url").toString()).openConnection();
            http.setRequestMethod("GET");
            http.connect();
            statusCode = http.getResponseCode();

            if (statusCode != 404 || statusCode != 500) {
                test.pass("statusCode: " + statusCode + " retornado para a URL: " + obj.get("url").toString());
            } else {
                test.fail(
                        "O statusCode retornado foi: " + statusCode + "<br>" +
                        "O serviço na url: " + obj.get("url").toString() + " esta fora do ar!"
                );
            }
        }
    }
}
