package com.reactnativeincodesdk;

import android.util.Log;

import com.incode.welcome_sdk.IncodeWelcome;

public class IncodeSdkInitializer {

  interface Listener {
    void onReady();
    void onLicenceVerificationError();
  }

  private final Listener listener;

  IncodeSdkInitializer(Listener listener) {
    this.listener = listener;
  }

  public void initialize() {
    IncodeWelcome.getInstance().verifyApiKey(new IncodeWelcome.VerifyListener() {
      @Override
      public void onVerified() {
        Log.i("IncSDK", "Licence check ✅");
        listener.onReady();
      }

      @Override
      public void onError() {
        Log.e("IncSDK", "Licence verification error occurred. Please make sure you have working internet connection and valid API key.");
        listener.onLicenceVerificationError();
      }
    });
  }
}
