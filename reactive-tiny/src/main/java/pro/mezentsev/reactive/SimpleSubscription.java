package pro.mezentsev.reactive;

import android.support.annotation.NonNull;

class SimpleSubscription implements Subscription {
    @NonNull
    private final Action mSubscriptionAction;

    public SimpleSubscription(@NonNull Action subscriptionAction) {
        mSubscriptionAction = subscriptionAction;
    }

    @Override
    public void unsubscribe() {
        mSubscriptionAction.unsubscribe();
    }

    @NonNull
    public Subscription start() {
        mSubscriptionAction.start();
        return this;
    }
}
