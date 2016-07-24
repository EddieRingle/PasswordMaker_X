package io.github.eddieringle.android.apps.passwordmaker.ext;

import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.Set;

import io.github.eddieringle.android.apps.passwordmaker.PMApplication;

public class ScopedBus {
    private final Bus bus = PMApplication.getAppBus();
    private final Set<Object> objects = new HashSet<Object>();
    private boolean active;

    public void register(Object obj) {
        if (objects.add(obj) && active) {
            bus.register(obj);
        }
    }

    public void unregister(Object obj) {
        if (objects.remove(obj) && active) {
            bus.unregister(obj);
        }
    }

    public void post(Object event) {
        bus.post(event);
    }

    public void paused() {
        active = false;
        for (Object obj : objects) {
            bus.unregister(obj);
        }
    }

    public void resumed() {
        active = true;
        for (Object obj : objects) {
            bus.register(obj);
        }
    }
}
