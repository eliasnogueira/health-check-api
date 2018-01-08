package br.com.sicredi;

import br.com.sicredi.support.ReadJSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;


@RunWith(Parameterized.class)
public class TestExecutor {

    @Parameterized.Parameter public String nome;
    @Parameterized.Parameter(1) public String url;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        return ReadJSON.read();
    }

    @Test
    public void healthCheckStatus() {
        assertTrue(healthCheckStatus(url));
    }

    private boolean healthCheckStatus(String url)  {
        boolean status = false;
        HttpURLConnection http;
        int statusCode = 0;

        try {
            http = (HttpURLConnection) new URL(url).openConnection();
            http.setRequestMethod("GET");
            http.connect();
            statusCode = http.getResponseCode();

            if (statusCode != 404 || statusCode != 500) {
                status = true;
            } else {
                status = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }
}

