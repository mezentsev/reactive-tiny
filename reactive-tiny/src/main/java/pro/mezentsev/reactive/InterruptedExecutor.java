package pro.mezentsev.reactive;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InterruptedExecutor implements Executor {
    @NonNull
    private static final ExecutorService ExecutorService = Executors.newCachedThreadPool();

    @Nullable
    private Future<?> mCurrentFuture;

    @NonNull
    private final Object mLock = new Object();

    @Override
    public void execute(@NonNull Runnable command) {
        synchronized (mLock) {
            mCurrentFuture = ExecutorService.submit(command);
        }
    }

    public boolean cancel() {
        boolean canceled = false;

        if (mCurrentFuture != null) {
            synchronized (mLock) {
                if (mCurrentFuture != null) {
                    canceled = mCurrentFuture.cancel(true);
                    mCurrentFuture = null;
                }
            }
        }

        return canceled;
    }
}
