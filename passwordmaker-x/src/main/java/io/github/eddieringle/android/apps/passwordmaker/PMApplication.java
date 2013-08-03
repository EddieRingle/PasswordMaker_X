package io.github.eddieringle.android.apps.passwordmaker;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class PMApplication extends Application {

    private static final Bus APP_BUS = new AndroidBus();

    public static Bus getAppBus() {
        return APP_BUS;
    }

    private static class AndroidBus extends Bus {
        private final Handler mMainThread = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                mMainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidBus.super.post(event);
                    }
                });
            }
        }
    }
}
