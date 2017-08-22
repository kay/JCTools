package org.jctools.proxy.logger;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.jctools.proxy.ProxyChannel;
import org.jctools.proxy.ProxyChannelFactory;
import org.jctools.proxy.WaitStrategy;

public class JcToolsProxyLoggerContext extends LoggerContext {
    private static class JcToolsProxyLogger extends Logger {
        private final Logger proxy;

        protected JcToolsProxyLogger(LoggerContext context, String name, MessageFactory messageFactory, Logger proxy) {
            super(context, name, messageFactory);
            this.proxy = proxy;
        }

        @Override
        public void logMessage(final String fqcn,
                final Level level,
                final Marker marker,
                final Message message,
                final Throwable thrown) {
            proxy.logMessage(fqcn, level, marker, message, thrown);
        }

    }

    private final ProxyChannel<Logger> proxyChannel;

    public JcToolsProxyLoggerContext(final String name) {
        super(name);
        this.proxyChannel = createProxyChannel(name);
    }

    public JcToolsProxyLoggerContext(final String name, final Object externalContext) {
        super(name, externalContext);
        this.proxyChannel = createProxyChannel(name);
    }

    public JcToolsProxyLoggerContext(final String name, final Object externalContext, final URI configLocn) {
        super(name, externalContext, configLocn);
        this.proxyChannel = createProxyChannel(name);
    }

    public JcToolsProxyLoggerContext(final String name, final Object externalContext, final String configLocn) {
        super(name, externalContext, configLocn);
        this.proxyChannel = createProxyChannel(name);
    }

    private static ProxyChannel<Logger> createProxyChannel(String name) {
        return ProxyChannelFactory.createMpscProxy(1024, Logger.class, new WaitStrategy() {

            @Override
            public int idle(int idleCounter) {
                return 0;
            }
        });
    }

    @Override
    protected Logger newInstance(final LoggerContext ctx, final String name, final MessageFactory messageFactory) {
        return new JcToolsProxyLogger(ctx, name, messageFactory, proxyChannel.proxy());
    }

    @Override
    public void setName(final String name) {
        super.setName("JcToolsProxyContext[" + name + "]");
    }

    Thread thread = null;
    volatile boolean shouldRun;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.LoggerContext#start()
     */
    @Override
    public void start() {
        shouldRun = true;
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (shouldRun) {
                    // proxyChannel.process(impl, 10);
                }
            }
        });
        thread.setName("JcToolsProxyContextWorker[" + getName() + "]");
        thread.start();

        super.start();
    }

    private void maybeStartHelper(final Configuration config) {
        // If no log4j configuration was found, there are no loggers
        // and there is no point in starting the channel (which takes up
        // significant memory and starts a thread).
        if (config instanceof DefaultConfiguration) {
            StatusLogger.getLogger().debug("[{}] Not starting Disruptor for DefaultConfiguration.", getName());
        } else {
            start();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.logging.log4j.core.LoggerContext#start(org.apache.logging.
     * log4j.core.config.Configuration)
     */
    @Override
    public void start(final Configuration config) {
        maybeStartHelper(config);
        super.start(config);
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        setStopping();
        shouldRun = false;
        thread.interrupt();
        // Don't join as process can block until limit is reached
        // TODO wtf

        // first stop Disruptor TODO
        // loggerDisruptor.stop(timeout, timeUnit);
        super.stop(timeout, timeUnit);
        return true;
    }

}
