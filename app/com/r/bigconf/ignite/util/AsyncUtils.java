package com.r.bigconf.ignite.util;

import org.apache.ignite.lang.IgniteFuture;

import java.util.concurrent.CompletableFuture;

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
}
