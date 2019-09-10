package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

import junit.framework.Assert;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReactiveUnitTest {
    @Test
    public void errorAndSuccess() throws InterruptedException {
        IntegerObservable integerObservable = new IntegerObservable(false);
        ThreadSubscriber subscriber1 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber1);

        Assert.assertNull(subscriber1.throwable);
        Assert.assertNotNull(subscriber1.res);

        integerObservable.hasException = true;
        ThreadSubscriber subscriber2 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber2);

        Assert.assertNull(subscriber2.res);
        assertNotNull(subscriber2.throwable);

        IntegerObservable integerObservable2 = new IntegerObservable(false);
        ThreadSubscriber subscriber3 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber3);

        Assert.assertNotNull(subscriber3.res);
        Assert.assertNull(subscriber3.throwable);
        Assert.assertTrue(subscriber3.completed);

        integerObservable2.hasException = true;
        ThreadSubscriber subscriber4 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber4);

        Assert.assertNull(subscriber4.res);
        Assert.assertNotNull(subscriber4.throwable);
        Assert.assertFalse(subscriber4.completed);

        assertEquals(subscriber1.resThread, subscriber2.resThread);
        assertEquals(subscriber1.resThread, subscriber3.resThread);
        assertEquals(subscriber1.resThread, subscriber4.resThread);
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
        Assert.assertNotNull(callable.intRes);
        Assert.assertNull(callable1.intRes);

        Thread.sleep(3500);
        Assert.assertNotNull(callable2.intRes);
        Thread.sleep(10000);
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
        assertNull(callable.intRes);
        subscription.unsubscribe();
    }

    private static class IntegerObservable implements ObservableOnSubscribe<Integer> {
        boolean hasException;

        public IntegerObservable(boolean hasException) {
            this.hasException = hasException;
        }

        @Override
        public void subscribe(@NonNull Subscriber<Integer> subscriber) throws Exception {
            if (hasException) {
                throw new Exception();
            }

            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onNext(3);
            subscriber.onComplete();
        }
    }

    private static class IntegerSleepObservable extends IntegerObservable {
        volatile Integer intRes;

        public IntegerSleepObservable(boolean hasException) {
            super(hasException);
        }

        @Override
        public void subscribe(@NonNull Subscriber<Integer> subscriber) throws Exception {
            if (hasException) {
                throw new Exception();
            }

            Thread.sleep(500);

            intRes = 1;
            subscriber.onNext(intRes);
            Thread.sleep(500);

            intRes++;
            subscriber.onNext(intRes);
            Thread.sleep(500);

            intRes++;
            subscriber.onNext(intRes);
            Thread.sleep(500);

            subscriber.onComplete();
        }
    }

    private class ThreadSubscriber implements Subscriber<Integer> {
        Thread resThread;
        Integer res;
        Throwable throwable;
        boolean completed = false;

        @Override
        public void onNext(Integer object) {
            resThread = Thread.currentThread();
            res = object;
        }

        @Override
        public void onComplete() {
            completed = true;
        }

        @Override
        public void onError(@NonNull Throwable t) {
            resThread = Thread.currentThread();
            throwable = t;
        }
    }
}