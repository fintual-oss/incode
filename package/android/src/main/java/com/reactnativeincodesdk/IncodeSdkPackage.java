package com.reactnativeincodesdk;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.List;

import androidx.annotation.NonNull;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;


public class IncodeSdkPackage implements ReactPackage {
  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    return singletonList(
      new IncodeSdkModule(reactContext)
    );
  }

  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return emptyList();
  }

}
