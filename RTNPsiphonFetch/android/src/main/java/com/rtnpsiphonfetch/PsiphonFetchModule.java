package com.rtnpsiphonfetch;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class PsiphonFetchModule extends NativePsiphonFetchSpec {

    public static String NAME = "RTNPsiphonFetch";
    private int listenerCount;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable connectionStateDisposable;

    private final PsiphonHelper psiphonHelper;

    PsiphonFetchModule(ReactApplicationContext context) {
        super(context);
        psiphonHelper = PsiphonHelper.getInstance(context);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Override
    public void fetch(String method, String url, String body, boolean usePsiphon, Promise promise) {
        psiphonHelper.fetch(method, url, body, usePsiphon, promise);
    }

    @Override
    public void startPsiphon(Promise promise) {
        try {
            psiphonHelper.startPsiphon();
            promise.resolve(null);
        } catch (RuntimeException e) {
            promise.reject(e);
        }
    }

    @Override
    public void stopPsiphon() {
        psiphonHelper.stopPsiphon();
    }

    @Override
    public void addListener(String eventType) {
        if (eventType.equals("PsiphonConnectionState")) {
            // Subscribe to psiphon connection observable and start emitting to React Native

            listenerCount += 1;
            // Once at least one listener is added, subscribe to the connection state relay
            // and start emitting to React Native
            if (connectionStateDisposable == null || connectionStateDisposable.isDisposed()) {
                connectionStateDisposable = psiphonHelper.getConnectionStateObservable()
                        .doOnNext(psiphonState -> emitPsiphonConnectionState(psiphonState.toString()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                compositeDisposable.add(connectionStateDisposable);
            }
        }
    }

    @Override
    public void removeListeners(double count) {
        listenerCount -= count;
        // Once all listeners are removed, dispose of the connection state relay
        if (listenerCount == 0) {
            compositeDisposable.dispose();
        }
    }

    private void emitPsiphonConnectionState(String state) {
        // do nothing if there are no listeners
        if (listenerCount == 0) {
            return;
        }
        WritableMap params = Arguments.createMap();
        params.putString("state", state);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("PsiphonConnectionState", params);
    }
}