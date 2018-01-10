package me.irbis.reactive;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
        private final Observable<T> mObservable;
        @NonNull
        private final ObservableOnSubscribe<T> mObservableOnSubscribe;
        @Nullable
        private Subscriber<T> mSubscriber;
        @NonNull
        final Object mSubscriberLock = new Object();

        SubscriptionAction(@NonNull Observable<T> observable,
                           @NonNull ObservableOnSubscribe<T> observableOnSubscribe,
                           @NonNull Subscriber<T> subscriber) {
            mObservable = observable;
            mObservableOnSubscribe = observableOnSubscribe;
            mSubscriber = subscriber;
        }

        @Override
        public void run() {
            try {
                mObservableOnSubscribe.subscribe(this);
            } catch (Exception e) {
                onError(e);
            }
        }

        @Override
        public void start() {
            mObservable.SubscribeExecutor.execute(this);
        }

        @Override
        public void unsubscribe() {
            synchronized (mSubscriberLock) {
                mSubscriber = null;
            }
        }

        @Override
        public void onNext(T object) {
            if (mSubscriber != null) {
                final T nextObject = object;
                mObservable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mSubscriber != null) {
                            synchronized (mSubscriberLock) {
                                if (mSubscriber != null) {
                                    mSubscriber.onNext(nextObject);
                                }
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onComplete() {
            if (mSubscriber != null) {
                mObservable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mSubscriber != null) {
                            synchronized (mSubscriberLock) {
                                if (mSubscriber != null) {
                                    mSubscriber.onComplete();
                                }
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onError(@NonNull Throwable t) {
            if (mSubscriber != null) {
                final Throwable nextThrowable = t;
                mObservable.ObserveExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mSubscriber != null) {
                            synchronized (mSubscriberLock) {
                                if (mSubscriber != null) {
                                    mSubscriber.onError(nextThrowable);
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
        private final Executor mExecutor = Executors.newSingleThreadExecutor();

        private WorkerExecutor() {
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mExecutor.execute(command);
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
        private final Handler mHandler;

        private MainThreadExecutor() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mHandler.post(command);
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
