package healthcheck.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadConfFile {

    public static String returnValue(String key) {
        String value = null;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("conf.properties"));
            value = properties.getProperty(key);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }
}
