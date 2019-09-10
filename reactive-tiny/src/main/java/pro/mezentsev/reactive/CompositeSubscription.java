package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeSubscription implements Subscription {

    @NonNull
    private final List<Subscription> subscriptions;

    public CompositeSubscription() {
        subscriptions = Collections.synchronizedList(new LinkedList<Subscription>());
    }

    public void add(@NonNull Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void unsubscribe() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }

        subscriptions.clear();
    }
}
