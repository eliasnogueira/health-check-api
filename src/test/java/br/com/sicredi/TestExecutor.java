package br.com.sicredi;

import br.com.sicredi.support.ReadJSON;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;

/**
 * Data Driven test that read the json file and create an execution for each
 * pair of name and url
 *
 * @author Elias Nogueira, Gregory Severo
 */
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
        try {
            assertTrue(healthCheckStatus(url));
        } catch (Throwable e) {
            // in the case of json validator throws an exception
            TestCase.fail(e.getMessage());
        }

    }

    /**
     * Create an HTTPConnection and get the responseCode. Based on that code the test will fail or not
     * @param url the url provided on json file
     * @return <i>true</i> if the endpoint is alive or <i>false</i> if not
     * @throws Exception
     */
    private boolean healthCheckStatus(String url) throws Exception {
        boolean status;
        HttpURLConnection http;
        int statusCode = 0;

        http = (HttpURLConnection) new URL(url).openConnection();
        http.setRequestMethod("GET");
        http.connect();
        statusCode = http.getResponseCode();

        if (statusCode != 404 || statusCode != 500) {
            status = true;
        } else {
            status = false;
        }

        return status;
    }
}

