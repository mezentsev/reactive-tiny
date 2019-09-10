package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ReactiveUnitTest {
    @Test
    @Ignore
    public void errorAndSuccess() throws InterruptedException {
        IntegerObservable integerObservable = new IntegerObservable(false);
        ThreadSubscriber subscriber1 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber1);

        assertNull(subscriber1.throwable);
        assertNotNull(subscriber1.res);

        integerObservable.hasException = true;
        ThreadSubscriber subscriber2 = new ThreadSubscriber();
        Observable.create(integerObservable)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber2);

        assertNull(subscriber2.res);
        assertNotNull(subscriber2.throwable);

        IntegerObservable integerObservable2 = new IntegerObservable(false);
        ThreadSubscriber subscriber3 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber3);

        assertNotNull(subscriber3.res);
        assertNull(subscriber3.throwable);
        assertTrue(subscriber3.completed);

        integerObservable2.hasException = true;
        ThreadSubscriber subscriber4 = new ThreadSubscriber();
        Observable.create(integerObservable2)
                .observeOn(Observable.WorkerExecutor.get())
                .subscribeOn(Observable.MainThreadExecutor.get())
                .subscribe(subscriber4);

        assertNull(subscriber4.res);
        assertNotNull(subscriber4.throwable);
        assertFalse(subscriber4.completed);

        assertEquals(subscriber1.resThread, subscriber2.resThread);
        assertEquals(subscriber1.resThread, subscriber3.resThread);
        assertEquals(subscriber1.resThread, subscriber4.resThread);
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