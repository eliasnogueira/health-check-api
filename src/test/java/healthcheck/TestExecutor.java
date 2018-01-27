/*
 * Copyright 2018 Elias Nogueira and Gregory Severo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package healthcheck;

import healthcheck.support.ReadConfFile;
import healthcheck.support.ReadJSON;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

/**
 * Data Driven test that read the json file and create an execution for each
 * pair of name and url
 *
 * @author Elias Nogueira, Gregory Severo
 */
@RunWith(Parameterized.class)
public class TestExecutor {

    @Parameterized.Parameter public String name;
    @Parameterized.Parameter(1) public String url;

    private static ExtentReports extentReports;
    private static ExtentHtmlReporter htmlReporter;
    private static ExtentTest testReport;
    private boolean testStatus;
    private String errorMessage;
    private static long connectionTimeout = 0;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        return ReadJSON.read();
    }

    @BeforeClass
    public static void setup() {
        ReadJSON.createReportDir();
        htmlReporter = new ExtentHtmlReporter("target/report/" + ReadJSON.getFileName() + ".html");
        extentReports = new ExtentReports();

        extentReports.attachReporter(htmlReporter);

        connectionTimeout = setConnectionTimeout();
    }

    @Test
    public void healthCheckStatus() {
        testReport = extentReports.createTest(name);

        try {
            assertTrue(healthCheckStatus(url));
            testStatus = true;

        } catch (Throwable e) {
            // in the case of json validator throws an exception
            errorMessage = e.getMessage().toString();
            TestCase.fail(errorMessage);
            testStatus = false;

        } finally {
            if (testStatus) {
                testReport.pass(url);
            } else {
                testReport.fail("<b>URL: </b>" + url + "<br>" +
                    "<b>Exception: </b>" + errorMessage);
            }
        }
    }

    @AfterClass
    public static void tearDown() {
        extentReports.flush();
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
        int statusCode;

        if (Boolean.parseBoolean(ReadConfFile.returnValue("http.setProxy"))) {
            setProxy();
        }

        http = (HttpURLConnection) new URL(url).openConnection();
        http.setRequestMethod("GET");
        http.setConnectTimeout((int)connectionTimeout);
        http.connect();

        statusCode = http.getResponseCode();

        if (statusCode == 404 || statusCode == 500) {
            status = false;
        } else {
            status = true;
        }

        System.out.println("name = " + name);
        System.out.println("url = " + url);
        System.out.println("-----");
        return status;
    }

    private static long setConnectionTimeout() {
        long connectionTimeoutParameter;

        if(System.getProperty("timeout") == null) {

            connectionTimeoutParameter = 10000;
        } else {
            connectionTimeoutParameter = TimeUnit.SECONDS.toMillis(Integer.parseInt(System.getProperty("timeout")));
        }

        return connectionTimeoutParameter;
    }

    private static void setProxy() {
        System.setProperty("http.proxyHost", ReadConfFile.returnValue("http.proxyHost"));
        System.setProperty("http.proxyPort", ReadConfFile.returnValue("http.proxyPort"));
        System.setProperty("https.proxyHost", ReadConfFile.returnValue("https.proxyHost"));
        System.setProperty("https.proxyPort", ReadConfFile.returnValue("https.proxyPort"));
    }
}

