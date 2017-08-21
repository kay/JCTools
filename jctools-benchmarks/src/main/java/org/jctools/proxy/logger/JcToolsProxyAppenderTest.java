package org.jctools.proxy.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JcToolsProxyAppenderTest {
    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j-jctools-async.xml");
        Logger logger1 = LogManager.getLogger(String.class);
        Logger logger2 = LogManager.getLogger(Long.class);

        logger1.info("Bob");
        logger2.info("Bob");
    }
}
