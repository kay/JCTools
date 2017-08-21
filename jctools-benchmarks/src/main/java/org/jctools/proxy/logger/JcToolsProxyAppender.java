package org.jctools.proxy.logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.jctools.proxy.ProxyChannel;
import org.jctools.proxy.ProxyChannelFactory;
import org.jctools.proxy.WaitStrategy;

@Plugin(name = "JcToolsAsync", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class JcToolsProxyAppender extends AbstractAppender {

    public interface JcToolsAppender {
        void append(LogEvent event);
    }

    public static final class JcToolsAppenderAdaptor implements JcToolsAppender, Appender {
        private final Appender appender;

        public JcToolsAppenderAdaptor(Appender appender) {
            super();
            this.appender = appender;
        }

        public State getState() {
            return appender.getState();
        }

        public void initialize() {
            appender.initialize();
        }

        public void append(LogEvent event) {
            appender.append(event);
        }

        public void start() {
            appender.start();
        }

        public void stop() {
            appender.stop();
        }

        public boolean isStarted() {
            return appender.isStarted();
        }

        public boolean isStopped() {
            return appender.isStopped();
        }

        public String getName() {
            return appender.getName();
        }

        public Layout<? extends Serializable> getLayout() {
            return appender.getLayout();
        }

        public boolean ignoreExceptions() {
            return appender.ignoreExceptions();
        }

        public ErrorHandler getHandler() {
            return appender.getHandler();
        }

        public void setHandler(ErrorHandler handler) {
            appender.setHandler(handler);
        }

    }

    private final AppenderRef appenderRef;
    private final int queueSize;
    private final Configuration configuration;

    private ProxyChannel<JcToolsAppender> proxyChannel;
    private volatile boolean shouldRun;
    private Thread thread;
    
    protected JcToolsProxyAppender(String name,
            Filter filter,
            boolean ignoreExceptions,
            AppenderRef appenderRef,
            int queueSize,
            final Configuration configuration) {
        super(name, filter, null, ignoreExceptions);
        this.appenderRef = appenderRef;
        this.queueSize = queueSize;
        this.configuration = configuration;
    }


    @Override
    public void append(LogEvent event) {
        LogEvent immutable = event.toImmutable();

        // event is often mutable so needs to be locked in to cross the thread
        try {
            proxyChannel.proxy().append(immutable);
        } finally {
            if (immutable == null) {
                System.out.println("After append " + Thread.currentThread() + " " + event.getClass() + " " + immutable);
            }
        }
    }

    @Override
    public void start() {
        final Map<String, Appender> map = configuration.getAppenders();
        final JcToolsAppender appender = new JcToolsAppenderAdaptor(map.get(appenderRef.getRef()));
        final ProxyChannel<JcToolsAppender> proxyChannel = ProxyChannelFactory.createMpscProxy(queueSize, JcToolsAppender.class, new WaitStrategy() {

            @Override
            public int idle(int idleCounter) {
                return 0;
            }
        });

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (shouldRun) {
                    proxyChannel.process(appender, 4096);
                }
            }
        });
        thread.setName("JcToolsAppender-" + getName() + "-"+thread.hashCode());
        thread.setDaemon(true);
        this.proxyChannel = proxyChannel;

        shouldRun = true;
        thread.start();
        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        setStopping();
        super.stop(timeout, timeUnit);
        shouldRun = false;
        thread.interrupt();
        try {
            thread.join(timeUnit.toMillis(timeout));
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<JcToolsProxyAppender> {

        @PluginElement("AppenderRef")
        @Required(message = "No appender references provided to AsyncAppender")
        private AppenderRef appenderRef;

        @PluginBuilderAttribute
        private int queueSize = 4096;

        @PluginBuilderAttribute
        @Required(message = "No name provided for AsyncAppender")
        private String name;

        @PluginElement("Filter")
        private Filter filter;

        @PluginConfiguration
        private Configuration configuration;

        @PluginBuilderAttribute
        private boolean ignoreExceptions = true;

        public Builder setAppenderRef(final AppenderRef appenderRef) {
            this.appenderRef = appenderRef;
            return this;
        }

        public Builder setQueueSize(final int queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder setFilter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        public Builder setConfiguration(final Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setIgnoreExceptions(final boolean ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
            return this;
        }

        @Override
        public JcToolsProxyAppender build() {
            return new JcToolsProxyAppender(name, filter, ignoreExceptions, appenderRef, queueSize, configuration);
        }
    }

}
