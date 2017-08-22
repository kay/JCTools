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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

@Threads(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JcToolsProxyAppenderLoggingBenchmark {

    @Param({ "jctools-proxy", "async-abq", "async-jctools", "file", "disruptor" })
    public String testCase;

    // Used in the report to display how many threads were used
    @Param({ "1" })
    public int producerThreads;

    public Logger logger;

    public String msg;
    public Object msgArg;

    @Setup(Level.Iteration)
    public void setupIteration() {
        if ("jctools-proxy".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-jctools-proxy.xml");
        } else if ("async-abq".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-async-abq.xml");
        } else if ("async-jctools".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-async-jctools.xml");
        } else if ("file".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-file.xml");
        } else if ("disruptor".equalsIgnoreCase(testCase)) {
            System.setProperty("log4j.configurationFile", "log4j-disruptor.xml");
        }

        logger = LogManager.getLogger(JcToolsProxyAppenderLoggingBenchmark.class);

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
                    .include(JcToolsProxyAppenderLoggingBenchmark.class.getSimpleName() + ".*thrptInfoWithArg.*")
                    .warmupIterations(3)
                    .measurementIterations(3)
                    .param("producerThreads", Integer.toString(threadCount))
                    .threads(threadCount)
                    .forks(1)
                    // .verbosity(VerboseMode.SILENT)
                    .build();
            allResults.addAll(new Runner(opt).run());

        }
        System.out.println();
        System.out.println("Summary results:");
        ResultFormatFactory.getInstance(ResultFormatType.TEXT, System.out).writeOut(allResults);
    }
}
