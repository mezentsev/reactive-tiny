package pro.mezentsev.reactive.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pro.mezentsev.reactive.CompositeSubscription;
import pro.mezentsev.reactive.InterruptedExecutor;
import pro.mezentsev.reactive.Observable;
import pro.mezentsev.reactive.ObservableOnSubscribe;
import pro.mezentsev.reactive.Subscriber;

public class ReactiveActivity extends Activity {
    @NonNull
    public static final String TAG = ReactiveActivity.class.getSimpleName();

    @NonNull
    private final CompositeSubscription subscription = new CompositeSubscription();

    @NonNull
    private final InterruptedExecutor interruptedExecutor = new InterruptedExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        final TextView timerView = (TextView) findViewById(R.id.timer_view);
        final Button buttonView = (Button) findViewById(R.id.button_view);
        final Button stopButtonView = (Button) findViewById(R.id.stop_button_view);

        buttonView.setOnClickListener(view -> {
            timerView.setText("START");

            subscription.unsubscribe();
            interruptedExecutor.cancel();

            subscription.add(Observable.create((ObservableOnSubscribe<String>) o -> {

                for (int i = 0; i < 10; i++) {
                    Log.d(TAG, "[EMITTER] onNext " + Thread.currentThread());
                    o.onNext(".");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        o.onError(e);
                        return;
                    }
                }

                Log.d(TAG, "[EMITTER] onError " + Thread.currentThread());
                o.onError(new Throwable("Problem detected"));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    o.onError(e);
                    return;
                }

                Log.d(TAG, "[EMITTER] onComplete " + Thread.currentThread());
                o.onComplete();
            })
                    .subscribeOn(interruptedExecutor)
                    .observeOn(Observable.MainThreadExecutor.get())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onNext(String object) {
                            Log.d(TAG, object + " [SUBSCRIBER] onNext " + Thread.currentThread());
                            CharSequence text = timerView.getText() + object;
                            timerView.setText(text);
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "[SUBSCRIBER] onComplete " + Thread.currentThread());

                            timerView.setText("DONE");
                        }

                        @Override
                        public void onError(@NonNull Throwable t) {
                            Log.d(TAG, "[SUBSCRIBER] onError " + Thread.currentThread());

                            timerView.setText(t.toString());
                        }
                    }));
        });

        stopButtonView.setOnClickListener(view -> {
            timerView.setText("STOPPED");
            subscription.unsubscribe();
            interruptedExecutor.cancel();
        });
    }
}
