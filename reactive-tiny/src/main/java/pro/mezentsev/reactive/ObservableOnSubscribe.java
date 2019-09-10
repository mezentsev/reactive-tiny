package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

public interface ObservableOnSubscribe<T> {
    void subscribe(@NonNull Subscriber<T> subscriber) throws Exception;
}
