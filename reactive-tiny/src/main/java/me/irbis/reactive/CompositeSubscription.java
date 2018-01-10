package me.irbis.reactive;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeSubscription implements Subscription {

    @NonNull
    private final List<Subscription> mSubscriptions;

    public CompositeSubscription() {
        mSubscriptions = Collections.synchronizedList(new LinkedList<Subscription>());
    }

    public void add(@NonNull Subscription subscription) {
        mSubscriptions.add(subscription);
    }

    @Override
    public void unsubscribe() {
        for (Subscription subscription : mSubscriptions) {
            subscription.unsubscribe();
        }

        mSubscriptions.clear();
    }
}
