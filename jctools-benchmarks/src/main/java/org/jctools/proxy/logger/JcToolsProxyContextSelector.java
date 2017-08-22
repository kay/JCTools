package org.jctools.proxy.logger;

import java.net.URI;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.util.PropertiesUtil;

public class JcToolsProxyContextSelector extends ClassLoaderContextSelector {

    /**
     * Returns {@code true} if the user specified this selector as the Log4jContextSelector, to make all loggers
     * asynchronous.
     * 
     * @return {@code true} if all loggers are asynchronous, {@code false} otherwise.
     */
    public static boolean isSelected() {
        return AsyncLoggerContextSelector.class.getName().equals(
                PropertiesUtil.getProperties().getStringProperty(Constants.LOG4J_CONTEXT_SELECTOR));
    }

    @Override
    protected LoggerContext createContext(final String name, final URI configLocation) {
        return new JcToolsProxyLoggerContext(name, null, configLocation);
    }

    @Override
    protected String toContextMapKey(final ClassLoader loader) {
        // LOG4J2-666 ensure unique name across separate instances created by webapp classloaders
        return "JcToolsAsyncContext@" + Integer.toHexString(System.identityHashCode(loader));
    }

    @Override
    protected String defaultContextName() {
        return "DefaultJcToolsAsyncContext@" + Thread.currentThread().getName();
    }
}
