package com.spectralogic.ds3cli.util;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel {
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService forPool = Executors.newFixedThreadPool(NUM_CORES * 2);

    public static <T> void For(final Iterable<T> elements, final Operation<T> operation) {
        try {
            forPool.invokeAll(createCallable(elements, operation));
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> Collection<Callable<Void>> createCallable(final Iterable<T> elements, final Operation<T> operation) {
        final List<Callable<Void>> callable = new LinkedList<>();
        for (final T elem : elements) {
            callable.add(new Callable<Void>() {
                @Override
                public Void call() throws IOException, SignatureException {
                    operation.perform(elem);
                    return null;
                }
            });
        }
        return callable;
    }

    public interface Operation<T> {
        void perform(T pParameter) throws IOException, SignatureException;
    }
}

