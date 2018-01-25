package pro.mezentsev.reactive;

public interface Action extends Runnable, Subscription {
    void start();
}
