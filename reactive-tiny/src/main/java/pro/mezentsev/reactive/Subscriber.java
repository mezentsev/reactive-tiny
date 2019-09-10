package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

public interface Subscriber<T> {
    void onNext(T object);
    void onComplete();
    void onError(@NonNull Throwable t);
}
