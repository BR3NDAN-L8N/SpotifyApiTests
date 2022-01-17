package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {

    private static Properties properties;

    private Props() {
    }

    public static Properties init() {
        if (properties == null) {
            properties = new Properties();
            try {
                properties.load(new FileInputStream("src/test/resources/hidden/API_config.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static String getClientId() {
        return init().getProperty("client_id");
    }

    public static String getClientSecret() {
        return init().getProperty("client_secret");
    }

    public static String getRefreshToken() {
        return init().getProperty("refresh_token");
    }

    public static String getGrantType() {
        return init().getProperty("grant_type");
    }
}
