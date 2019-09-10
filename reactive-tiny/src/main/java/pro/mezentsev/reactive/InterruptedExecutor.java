package pro.mezentsev.reactive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InterruptedExecutor implements Executor {
    @NonNull
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @Nullable
    private Future<?> currentFuture;

    @NonNull
    private final Object lock = new Object();

    @Override
    public void execute(@NonNull Runnable command) {
        synchronized (lock) {
            currentFuture = EXECUTOR_SERVICE.submit(command);
        }
    }

    public boolean cancel() {
        boolean canceled = false;

        if (currentFuture != null) {
            synchronized (lock) {
                if (currentFuture != null) {
                    canceled = currentFuture.cancel(true);
                    currentFuture = null;
                }
            }
        }

        return canceled;
    }
}
