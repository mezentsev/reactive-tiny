package pro.mezentsev.reactive;

import androidx.annotation.NonNull;

class SimpleSubscription implements Subscription {
    @NonNull
    private final Action subscriptionAction;

    public SimpleSubscription(@NonNull Action subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    @Override
    public void unsubscribe() {
        subscriptionAction.unsubscribe();
    }

    @NonNull
    public Subscription start() {
        subscriptionAction.start();
        return this;
    }
}
