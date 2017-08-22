package org.jctools.proxy.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JcToolsProxyAppenderLoggingBenchmark {

    @Param({ "jctools", "async", "file", "disruptor" })
    public String testCase;

    public Logger logger;

    public String msg;
    public Object msgArg;

    @Setup(Level.Iteration)
    public void setupIteration() {
        if ("jctools".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-jctools-async.xml");
        } else if ("async".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-async.xml");
        } else if ("file".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-file.xml");
        } else if ("disruptor".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-disruptor.xml");
            System.setProperty("Log4jContextSelector",
                    "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        }

        logger = LogManager.getLogger(JcToolsProxyAppender.class);

        msg = "Hello World";
        msgArg = "Good";
    }

    @Benchmark
    public void thrptInfo() {
        logger.info(msg);
    }

    @Benchmark
    public void thrptInfoWithArg() {
        logger.info("Hello {} World", msgArg);
    }

    public static void main(String[] args) throws Exception {
        Collection<RunResult> allResults = new ArrayList<RunResult>();
        for (int threadCount : new int[] { 2, 1 }) {
            final Options opt = new OptionsBuilder()
                    .include(JcToolsProxyAppenderLoggingBenchmark.class.getSimpleName())
                    .warmupIterations(5)
                    .measurementIterations(5)
                    .threads(threadCount)
                    .forks(2)
                    .build();
            allResults.addAll(new Runner(opt).run());

        }

        ResultFormatFactory.getInstance(ResultFormatType.TEXT, System.out).writeOut(allResults);
    }
}
