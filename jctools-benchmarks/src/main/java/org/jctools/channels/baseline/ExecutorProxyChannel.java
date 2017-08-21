package org.jctools.channels.baseline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jctools.channels.baseline.ExecutorChannelBenchmark.BenchIFace;
import org.jctools.channels.baseline.ExecutorChannelBenchmark.CustomType;
import org.jctools.proxy.ProxyChannel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

class ExecutorProxyChannel implements ProxyChannel<BenchIFace> {

    final ExecutorService executor;
    final BenchIFace impl;

    volatile int processed = 0;
    int threadLocalProcessedCount = 0;

    public ExecutorProxyChannel(final BenchIFace impl) {
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
        this.impl = impl;
    }

    @Override
    public BenchIFace proxyInstance(final BenchIFace impl) {
        throw new IllegalStateException("Not implemented");
    }
    
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public BenchIFace proxy() {
        return new BenchIFace() {

            @Override
            public void unalignedPrimitiveArgs(final long l1, final double d1, final long l2, final double d2, final long l3, final double d3, final long l4,
                    final double d4, final long l5, final double d5, final long l6, final double d6, final long l7, final double d7, final long l8, final double d8, final int i) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.unalignedPrimitiveArgs(l1, d1, l2, d2, l3, d3, l4, d4, l5, d5, l6, d6, l7, d7, l8, d8, i);
                        processed++;
                    }
                });
            }

            @Override
            public void twoMixedLengthPrimitiveArgs(final int x, final long y) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.twoMixedLengthPrimitiveArgs(x, y);
                        processed++;
                    }
                });
            }

            @Override
            public void tenMixedArgs(final int i, final Object o, final long l, final CustomType c0, final double d, final CustomType c1, final float f,
                    final CustomType c2, final boolean b, final CustomType c3) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.tenMixedArgs(i, o, l, c0, d, c1, f, c2, b, c3);
                        processed++;
                    }
                });
            }

            @Override
            public void oneReferenceArg(final CustomType x) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.oneReferenceArg(x);
                        processed++;
                    }
                });

            }

            @Override
            public void onePrimitiveArg(final int x) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.onePrimitiveArg(x);
                        processed++;
                    }
                });

            }

            @Override
            public void oneObjectArg(final Object x) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.oneObjectArg(x);
                        processed++;
                    }
                });

            }

            @Override
            public void noArgs() {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.noArgs();
                        processed++;
                    }
                });

            }

            @Override
            public void alignedPrimitiveArgs(final int i, final long l1, final double d1, final long l2, final double d2, final long l3, final double d3, final long l4,
                    final double d4, final long l5, final double d5, final long l6, final double d6, final long l7, final double d7, final long l8, final double d8) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        impl.alignedPrimitiveArgs(i, l1, d1, l2, d2, l3, d3, l4, d4, l5, d5, l6, d6, l7, d7, l8, d8);
                        processed++;
                    }
                });

            }
        };
    }

    @Override
    public int process(final BenchIFace impl, final int limit) {
        final int v = processed;
        final int diff = v - threadLocalProcessedCount;
        threadLocalProcessedCount = v;
        return diff;
    }

    @Override
    public int size() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int capacity() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("Not implemented");
    }
}
