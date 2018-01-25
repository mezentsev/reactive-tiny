package pro.mezentsev.reactive;

import android.support.annotation.NonNull;

public interface ObservableOnSubscribe<T> {
    void subscribe(@NonNull Subscriber<T> subscriber) throws Exception;
}
