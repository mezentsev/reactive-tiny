package me.irbis.reactive;

public interface Action extends Runnable, Subscription {
    void start();
}
