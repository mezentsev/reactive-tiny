package me.irbis.reactive;

import android.support.annotation.NonNull;

import junit.framework.Assert;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner.class)
public class ReactiveUnitTest {
    @Test
    public void errorAndSuccess() throws InterruptedException {
        IntegerObservable integerObservable = new IntegerObservable(false);
        ThreadSubscriber subscriber1 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber1);

        Assert.assertNull(subscriber1.mThrowable);
        Assert.assertNotNull(subscriber1.mRes);

        integerObservable.mHasException = true;
        ThreadSubscriber subscriber2 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber2);

        Assert.assertNull(subscriber2.mRes);
        Assert.assertNotNull(subscriber2.mThrowable);

        IntegerObservable integerObservable2 = new IntegerObservable(false);
        ThreadSubscriber subscriber3 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber3);

        Assert.assertNotNull(subscriber3.mRes);
        Assert.assertNull(subscriber3.mThrowable);
        Assert.assertTrue(subscriber3.mCompleted);

        integerObservable2.mHasException = true;
        ThreadSubscriber subscriber4 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber4);

        Assert.assertNull(subscriber4.mRes);
        Assert.assertNotNull(subscriber4.mThrowable);
        Assert.assertFalse(subscriber4.mCompleted);

        assertEquals(subscriber1.mResThread, subscriber2.mResThread);
        assertEquals(subscriber1.mResThread, subscriber3.mResThread);
        assertEquals(subscriber1.mResThread, subscriber4.mResThread);
    }

    @Test
    public void unsubscribe() throws InterruptedException {
        IntegerSleepObservable callable = new IntegerSleepObservable(false);
        IntegerSleepObservable callable1 = new IntegerSleepObservable(false);
        IntegerSleepObservable callable2 = new IntegerSleepObservable(false);

        ThreadSubscriber subscriber = new ThreadSubscriber();
        Subscription subscription = Observable.create(callable)
                .observeOn(Observable.MainThreadExecutor.get())
                .subscribeOn(Observable.WorkerExecutor.get())
                .subscribe(subscriber);
        Thread.sleep(100);
        subscription.unsubscribe();

        subscription = Observable.create(callable1)
                .observeOn(Observable.MainThreadExecutor.get())
                .subscribeOn(Observable.WorkerExecutor.get())
                .subscribe(subscriber);
        Thread.sleep(100);
        subscription.unsubscribe();

        subscription = Observable.create(callable2)
                .observeOn(Observable.MainThreadExecutor.get())
                .subscribeOn(Observable.WorkerExecutor.get())
                .subscribe(subscriber);

        Thread.sleep(2000);
        Assert.assertNotNull(callable.mIntRes);
        Assert.assertNull(callable1.mIntRes);

        Thread.sleep(3500);
        Assert.assertNotNull(callable2.mIntRes);
    }

    @Test
    public void ucancel() throws InterruptedException {
        IntegerSleepObservable callable = new IntegerSleepObservable(false);

        InterruptedExecutor interruptedExecutor = new InterruptedExecutor();

        ThreadSubscriber subscriber = new ThreadSubscriber();
        Subscription subscription = Observable.create(callable)
                .observeOn(Observable.MainThreadExecutor.get())
                .subscribeOn(interruptedExecutor)
                .subscribe(subscriber);

        assertTrue(interruptedExecutor.cancel());
        Thread.sleep(800);
        assertNull(callable.mIntRes);
        subscription.unsubscribe();
    }

    private static class IntegerObservable implements ObservableOnSubscribe<Integer> {
        boolean mHasException;

        public IntegerObservable(boolean hasException) {
            mHasException = hasException;
        }

        @Override
        public void subscribe(@NonNull Subscriber<Integer> subscriber) throws Exception {
            if (mHasException) {
                throw new Exception();
            }

            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onNext(3);
            subscriber.onComplete();
        }
    }

    private static class IntegerSleepObservable extends IntegerObservable {
        volatile Integer mIntRes;

        public IntegerSleepObservable(boolean hasException) {
            super(hasException);
        }

        @Override
        public void subscribe(@NonNull Subscriber<Integer> subscriber) throws Exception {
            if (mHasException) {
                throw new Exception();
            }

            Thread.sleep(500);

            mIntRes = 1;
            subscriber.onNext(mIntRes);
            Thread.sleep(500);

            mIntRes++;
            subscriber.onNext(mIntRes);
            Thread.sleep(500);

            mIntRes++;
            subscriber.onNext(mIntRes);
            Thread.sleep(500);

            subscriber.onComplete();
        }
    }

    private class ThreadSubscriber implements Subscriber<Integer> {
        Thread mResThread;
        Integer mRes;
        Throwable mThrowable;
        boolean mCompleted = false;

        @Override
        public void onNext(Integer object) {
            mResThread = Thread.currentThread();
            mRes = object;
        }

        @Override
        public void onComplete() {
            mCompleted = true;
        }

        @Override
        public void onError(@NonNull Throwable t) {
            mResThread = Thread.currentThread();
            mThrowable = t;
        }
    }
}