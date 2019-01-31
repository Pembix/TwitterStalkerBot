package com.pembix.twitterstalkerapp.utils;

import org.apache.log4j.Logger;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public static String ACCESS_KEY;
    public static String ACCESS_SECRET;
    public static AccessToken ACCESS_TOKEN;
    public static String OAUTH_CONSUMER_KEY;
    public static String OAUTH_CONSUMER_SECRET;
    public static String BOT_USERNAME;
    public static String BOT_TOKEN;
    public static Boolean USE_MONGO_DB;
    public static String MONGO_DB_URL;
    public static String PERSISTENCE_UNIT;
    public static Properties STRINGS; //access properties by STRINGS.getProperty("PROPERTY")

    private final static Logger logger = Logger.getLogger(Config.class);

    static {
        Properties propConfig = new Properties();
        STRINGS = new Properties();
        InputStream input = null;

        try {
            String configProperties = "config.properties";
            String strings = "strings.properties";

            input = Config.class.getClassLoader().getResourceAsStream(configProperties);
            if (input == null) {
                logger.debug("Sorry, unable to find " + configProperties);
            } else {
                propConfig.load(input);

                logger.info("Got config: " + propConfig.stringPropertyNames());

                ACCESS_KEY = propConfig.getProperty("ACCESS_KEY");
                ACCESS_SECRET = propConfig.getProperty("ACCESS_SECRET");
                ACCESS_TOKEN = new AccessToken(ACCESS_KEY, ACCESS_SECRET);
                OAUTH_CONSUMER_KEY = propConfig.getProperty("OAUTH_CONSUMER_KEY");
                OAUTH_CONSUMER_SECRET = propConfig.getProperty("OAUTH_CONSUMER_SECRET");
                BOT_USERNAME = propConfig.getProperty("BOT_USERNAME");
                BOT_TOKEN = propConfig.getProperty("BOT_TOKEN");
                USE_MONGO_DB = Boolean.valueOf(propConfig.getProperty("USE_MONGO_DB"));
                MONGO_DB_URL = propConfig.getProperty("MONGO_DB_URL");
                PERSISTENCE_UNIT = propConfig.getProperty("PERSISTENCE_UNIT");
            }

            input = Config.class.getClassLoader().getResourceAsStream(strings);
            if (input == null) {
                logger.debug("Sorry, unable to find " + strings);
            } else {
                STRINGS.load(input);
                logger.info("Got config: " + STRINGS.stringPropertyNames());
            }
        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    public static String getString(String propertyName) {
        return STRINGS.getProperty(propertyName, "<error>");
    }
}
