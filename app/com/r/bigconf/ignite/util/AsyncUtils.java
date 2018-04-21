package com.r.bigconf.ignite.util;

import com.google.common.collect.Lists;
import org.apache.ignite.lang.IgniteFuture;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncUtils {

    public static <T> CompletableFuture<T> toCF(IgniteFuture<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.listen((completedIgniteFuture) -> {
            Throwable exception = null;
            T result = null;
            try {
                result = completedIgniteFuture.get();
            } catch (Throwable t) {
                exception = t;
            }
            if (exception != null) {
                completableFuture.completeExceptionally(exception);
            } else {
                completableFuture.complete(result);
            }
        });
        return completableFuture;
    }
    public static <T> CompletableFuture<Optional<T>> toCFO(IgniteFuture<T> future) {
        CompletableFuture<Optional<T>> completableFuture = new CompletableFuture<>();
        future.listen((completedIgniteFuture) -> {
            Throwable exception = null;
            T result = null;
            try {
                result = completedIgniteFuture.get();
            } catch (Throwable t) {
                exception = t;
            }
            if (exception != null) {
                completableFuture.completeExceptionally(exception);
            } else {
                completableFuture.complete(Optional.ofNullable(result));
            }
        });
        return completableFuture;
    }

    public static <T,R> CompletableFuture<List<T>> toCF(Iterator<R> iterator, Function<R,T> extractor) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        List<T> result = Lists.newArrayList();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    result.add(extractor.apply(iterator.next()));
                    CompletableFuture.runAsync(this);
                } else {
                    future.complete(result);
                }
            }
        };
        CompletableFuture.runAsync(runnable);
        return future;
    }

}
