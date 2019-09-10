package pro.mezentsev.reactive;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Observable<T> {
    @NonNull
    @SuppressWarnings("WeakerAccess")
    protected final ObservableOnSubscribe<T> ObservableOnSubscribe;

    @NonNull
    @SuppressWarnings("WeakerAccess")
    protected Executor SubscribeExecutor = DirectExecutor.get();

    @NonNull
    @SuppressWarnings("WeakerAccess")
    protected Executor ObserveExecutor = DirectExecutor.get();

    @SuppressWarnings("WeakerAccess")
    Observable(@NonNull ObservableOnSubscribe<T> onSubscribe) {
        ObservableOnSubscribe = onSubscribe;
    }

    @NonNull
    public static <T> Observable<T> create(@NonNull ObservableOnSubscribe<T> observableOnSubscribe) {
        return new Observable<>(observableOnSubscribe);
    }

    @NonNull
    public Observable<T> subscribeOn(@NonNull Executor executor) {
        SubscribeExecutor = executor;

        return this;
    }

    @NonNull
    public Observable<T> observeOn(@NonNull Executor executor) {
        ObserveExecutor = executor;
        return this;
    }

    @NonNull
    public Subscription subscribe(@NonNull Subscriber<T> subscriber) {
        return new SimpleSubscription(new SubscriptionAction<>(this, ObservableOnSubscribe, subscriber)).start();
    }

    private static final class SubscriptionAction<T> implements Action, Subscriber<T> {
        @NonNull
        private final Observable<T> observable;
        @NonNull
        private final ObservableOnSubscribe<T> observableOnSubscribe;

        @NonNull
        private final Object subscriberLock = new Object();

        @Nullable
        private Subscriber<T> subscriber;

        SubscriptionAction(@NonNull Observable<T> observable,
                           @NonNull ObservableOnSubscribe<T> observableOnSubscribe,
                           @NonNull Subscriber<T> subscriber) {
            this.observable = observable;
            this.observableOnSubscribe = observableOnSubscribe;
            this.subscriber = subscriber;
        }

        @Override
        public void run() {
            try {
                observableOnSubscribe.subscribe(this);
            } catch (Exception e) {
                onError(e);
            }
        }

        @Override
        public void start() {
            observable.SubscribeExecutor.execute(this);
        }

        @Override
        public void unsubscribe() {
            synchronized (subscriberLock) {
                subscriber = null;
            }
        }

        @Override
        public void onNext(T object) {
            if (subscriber != null) {
                final T nextObject = object;
                observable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (subscriber != null) {
                            synchronized (subscriberLock) {
                                if (subscriber != null) {
                                    subscriber.onNext(nextObject);
                                }
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onComplete() {
            if (subscriber != null) {
                observable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (subscriber != null) {
                            synchronized (subscriberLock) {
                                if (subscriber != null) {
                                    subscriber.onComplete();
                                }
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onError(@NonNull Throwable t) {
            if (subscriber != null) {
                final Throwable nextThrowable = t;
                observable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (subscriber != null) {
                            synchronized (subscriberLock) {
                                if (subscriber != null) {
                                    subscriber.onError(nextThrowable);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public static class WorkerExecutor implements Executor {
        @NonNull
        public static Executor get() {
            return INSTANCE;
        }

        private static final WorkerExecutor INSTANCE = new WorkerExecutor();

        @NonNull
        private final Executor executor = Executors.newSingleThreadExecutor();

        private WorkerExecutor() {
        }

        @Override
        public void execute(@NonNull Runnable command) {
            executor.execute(command);
        }
    }

    public static class MainThreadExecutor implements Executor {
        @NonNull
        public static Executor get() {
            return INSTANCE;
        }

        @NonNull
        private static final MainThreadExecutor INSTANCE = new MainThreadExecutor();

        @NonNull
        private final Handler handler;

        private MainThreadExecutor() {
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }

    public static class DirectExecutor implements Executor {
        @NonNull
        public static Executor get() {
            return INSTANCE;
        }

        private DirectExecutor() {
        }

        @NonNull
        private static final DirectExecutor INSTANCE = new DirectExecutor();

        @Override
        public void execute(@NonNull Runnable command) {
            command.run();
        }
    }
}
