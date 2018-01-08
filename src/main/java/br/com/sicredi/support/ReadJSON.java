package br.com.sicredi.support;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReadJSON {

    public static List<Object[]> read() throws Exception {
        JSONParser parser = new JSONParser();
        Object arquivo = parser.parse(new FileReader("health_check.json"));
        JSONObject jsonObject = (JSONObject) arquivo;

        List<Object[]> dados = new ArrayList<Object[]>();

        JSONArray testes = (JSONArray) jsonObject.get("testes");

        for (int i = 0; i < testes.size(); i++) {
            String[] informacoes = new String[2];
            JSONObject obj = (JSONObject) testes.get(i);
            informacoes[0] = obj.get("nome").toString();
            informacoes[1] = obj.get("url").toString();
            dados.add(informacoes);
        }

        return dados;
    }
}
