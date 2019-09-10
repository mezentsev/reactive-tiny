# Reactive Tiny
[![Build Status](https://travis-ci.org/mezentsev/reactive-tiny.svg?branch=master)](https://travis-ci.org/xCryogenx/reactive-tiny)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[ ![Download](https://api.bintray.com/packages/mezentsev/maven/reactive-tiny/images/download.svg) ](https://bintray.com/mezentsev/maven/reactive-tiny/_latestVersion)

Reactive Tiny is a lightweight library for composing asynchronous and event-based programs using observable sequences. It's light version of RxJava.

## Getting started

The first step is to include reactive-tiny into your project, for example, as a Gradle compile dependency:

```groovy
dependencies {
    implementation 'pro.mezentsev:reactive-tiny:1.0.2'
}
```

One of the common use cases is to run some computation, network request on a background thread and show the results (or error) on the UI thread.

Create new `CompositeSubscription`

```java
CompositeSubscription subscription = new CompositeSubscription();
```

Add new `Observable` to your `CompositeSubscription`, subscribe computation on a background thread and showing the results on the UI thread like this:

```java
subscription.add(
     Observable.create(
           new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull Subscriber<String> o) throws Exception {
                       Thread.sleep(1000); //  imitate expensive computation
                       o.onNext("1");
                       o.onComplete();
                }
           })
           .subscribeOn(Observable.WorkerExecutor.get())
           .observeOn(Observable.MainThreadExecutor.get())
           .subscribe(new Subscriber<String>() {
                @Override
                public void onNext(String object) {
                      // onNext
                }

                @Override
                public void onComplete() {
                      // onComplete
                }

                @Override
                public void onError(@NonNull Throwable t) {
                      // onError
                }
            }));
```

Typically, you can move computations or blocking IO to some other thread via subscribeOn. Once the data is ready, you can make sure they get processed on the foreground or GUI thread via observeOn.

If you actually don't need to observe computation from `CompositeSubscription`, use:

```java
subscription.unsubscribe();
```

but this operation doesn't interrupt created threads in subscriber. You can use `InterruptedExecutor` with `cancel` operation. It's trully interrupt all threads running on the executor.

## License
```
MIT License

Copyright (c) 2018 Vadim Mezentsev (@mezentsev)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
