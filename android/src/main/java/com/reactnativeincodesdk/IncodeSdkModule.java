package com.reactnativeincodesdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.incode.welcome_sdk.IncodeWelcome;
import com.incode.welcome_sdk.IncodeWelcome.IDResultsFetchMode;
import com.incode.welcome_sdk.OnboardingConfigV2;
import com.incode.welcome_sdk.OnboardingFlowConfig;
import com.incode.welcome_sdk.OnboardingValidationModule;
import com.incode.welcome_sdk.listeners.ApproveListener;
import com.incode.welcome_sdk.listeners.FinishOnboardingListener;
import com.incode.welcome_sdk.listeners.GetUserScoreListener;
import com.incode.welcome_sdk.listeners.OnboardingSessionListener;
import com.incode.welcome_sdk.listeners.SelfieScanListener;
import com.incode.welcome_sdk.modules.SelfieScan;
import com.incode.welcome_sdk.modules.exceptions.ModuleConfigurationException;
import com.incode.welcome_sdk.results.ApproveResult;
import com.incode.welcome_sdk.results.SelfieScanResult;
import com.incode.welcome_sdk.results.UserScoreResult;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

import static java.lang.String.format;


public class IncodeSdkModule extends ReactContextBaseJavaModule {

  private final BaseActivityEventListener activityListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      super.onActivityResult(activity, requestCode, resultCode, data);
      Log.d("INC::MODULE", format("Started activity %s result code: %s; data: %s", activity.getIntent(), resultCode, data));
    }

    @Override
    public void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
    }
  };

  public IncodeSdkModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addActivityEventListener(activityListener);
  }

  @NonNull
  @Override
  public String getName() {
    return "IncodeSdk";
  }

  @ReactMethod
  public void initialize(ReadableMap config, Promise promise) {
    Log.d("INC", "SDK::initialize");
    Activity currentActivity = getCurrentActivity();
    if (currentActivity == null) {
      throw new RuntimeException("INCSdk native part not properly initialized");
    }

    ReadableMap apiConfig = config.getMap("apiConfig");
    if (apiConfig == null) {
      rejectBadConfig(promise);
      return;
    }
    String apiUrl = apiConfig.getString("url");
    String apiKey = apiConfig.getString("key");

    if (apiKey == null || apiUrl == null) {
      rejectBadConfig(promise);
      return;
    }

    boolean disableHookCheck = false;
    if (config.hasKey("disableHookCheck")) {
      disableHookCheck = config.getBoolean("disableHookCheck");
    }

    boolean sdkInitialized = true;
    Throwable error = null;
    String errorCode = "";
    try {
      IncodeWelcome.Builder builder = new IncodeWelcome.Builder(currentActivity.getApplication(), apiUrl, apiKey);
      builder
        .setLoggingEnabled(true)
        .setTestModeEnabled(config.getBoolean("testMode"))
        .disableHookCheck(disableHookCheck);

      if (config.hasKey("sdkMode")) {
        if ("standard".equalsIgnoreCase(config.getString("sdkMode"))) {
          builder.setSdkMode(com.incode.welcome_sdk.SdkMode.STANDARD);
        } else if ("captureOnly".equalsIgnoreCase(config.getString("sdkMode"))) {
          builder.setSdkMode(com.incode.welcome_sdk.SdkMode.CAPTURE_ONLY);
        }
      }

      builder.build();
    } catch (IllegalStateException exc) {
      error = exc;
      sdkInitialized = false;
      String errorMsg = exc.getMessage();
      if ("Emulator detected, emulators aren't supported in non test mode!".equals(errorMsg)) {
        errorCode = "Incd::EmulatorDetected";
      } else if ("Root access detected, rooted devices aren't supported in non test mode!".equals(errorMsg)) {
        errorCode = "Incd::RootDetected";
      } else if ("Hooking framework detected, devices with hooking frameworks aren't supported in non test mode!".equals(errorMsg)) {
        errorCode = "Incd::HookDetected";
      } else if ("Please disable test mode before deploying to a real device!".equals(errorMsg)) {
        errorCode = "Incd::TestModeDetected";
      }
    } finally {
      if (sdkInitialized) {
        new IncodeSdkInitializer(new IncodeSdkInitializer.Listener() {

          @Override
          public void onReady() {
            promise.resolve("SDK ready");
          }

          @Override
          public void onLicenceVerificationError() {
            promise.reject("Incd::Licence", "Licence verification failed.");
          }
        }).initialize();
      } else {
        promise.reject(errorCode, error);
      }
    }

  }

  private void rejectBadConfig(Promise promise) {
    promise.reject("Incd:ConfigError", "Missing or incomplete apiConfig", new IllegalArgumentException("Missing API configuration"));
  }

  @ReactMethod
  public void startOnboarding(ReadableMap configuration, Promise promise) {
    try {
      ReactOnboardingListener onboardingListener = new ReactOnboardingListener(promise, getReactApplicationContext());
      OnboardingConfigV2 config = configFactory(configuration).getConfig();
      if (getCurrentActivity() != null) {
        getCurrentActivity().runOnUiThread(() -> IncodeWelcome.getInstance().startOnboardingV2(getCurrentActivity(), config, onboardingListener));

      }
    } catch (ModuleConfigurationException e) {
      promise.reject("Incd::OnboardingConfigException", e);
    }
  }

  @ReactMethod
  public void createOnboardingSession(ReadableMap configuration, Promise promise) {
    OnboardingSessionListener onboardingSessionListener = new OnboardingSessionListener() {
      @Override
      public void onOnboardingSessionCreated(String token, String interviewId, String regionId) {
        WritableMap argMap = Arguments.createMap();
        argMap.putString("token", token);
        argMap.putString("interviewId", interviewId);
        argMap.putString("regionId", regionId);
        promise.resolve(argMap);
      }

      @Override
      public void onError(Throwable throwable) {
        promise.reject(throwable);
      }

      @Override
      public void onUserCancelled() {
        userCancelled(promise, "Incd::UserCancelled");
      }
    };
    List<OnboardingValidationModule> validationModules = configToValidationModules(configuration);

    try {
      OnboardingConfigV2 config = new OnboardingConfigV2.OnboardingConfigBuilderV2().setRegion("ALL").build();
      IncodeWelcome.getInstance().createNewOnboardingSession(config,
        validationModules,
        onboardingSessionListener);
    } catch (ModuleConfigurationException e) {
      e.printStackTrace();
    }

  }

  private OnboardingConfigFactory configFactory(ReadableMap configuration) throws ModuleConfigurationException {
    return OnboardingConfigFactory.NEW_FROM(configuration.getArray("config"), configuration.getString("interviewId"), configuration.getString("configurationId"),
      new UserPreferences(getReactApplicationContext()));
  }

  @ReactMethod
  public void startOnboardingSection(ReadableMap configuration, Promise promise) {
  ReactOnboardingListener onboardingListener = new ReactOnboardingListener(promise, getReactApplicationContext());
    String interviewId = configuration.getString("interviewId");

    try {
      OnboardingConfigV2 config = configFactory(configuration).getConfig();

      IncodeWelcome.getInstance().startOnboardingSection(getCurrentActivity(), interviewId, config, onboardingListener);
    } catch (ModuleConfigurationException e) {
      promise.reject("Incd::OnboardingConfigException", e);
    }
  }

  private List<OnboardingValidationModule> configToValidationModules(ReadableMap configuration) {
    return Arrays.asList(OnboardingValidationModule.id);
  }

  @ReactMethod
  public void finishOnboardingFlow(Promise promise) {
    IncodeWelcome.getInstance().finishOnboarding(getReactApplicationContext(), new FinishOnboardingListener() {
      @Override
      public void onOnboardingFinished() {
        promise.resolve("");
      }

      @Override
      public void onError(Throwable throwable) {
        promise.reject(throwable);
      }

      @Override
      public void onUserCancelled() {
        userCancelled(promise, "Incd::UserCancelled");
      }
    });
  }

  @ReactMethod
  public void approve(ReadableMap configuration, Promise promise) {
    boolean forceApproval = configuration.getBoolean("forceApproval");
    Log.d("INC", "Starting approval; forcing it: " + forceApproval);

    IncodeWelcome.getInstance().startApprove(getCurrentActivity(), null, false, false, forceApproval, new ApproveListener() {
      @Override
      public void onApproveCompleted(ApproveResult approveResult) {
        WritableMap map = Arguments.createMap();
        map.putString("approved", approveResult.isSuccess() ? "approved" : "failed");
        map.putString("id", approveResult.getUuid());
        map.putString("customerToken", approveResult.getToken());
        promise.resolve(map);
        Log.d("INC", "Approved " + approveResult.isSuccess());
      }

      @Override
      public void onError(Throwable throwable) {
        promise.reject(throwable);
        Log.d("INC", "approval error", throwable);
      }

      @Override
      public void onUserCancelled() {
        userCancelled(promise, "Incd::APPROVE::UserCancelled");
      }
    });
  }

  @ReactMethod
  public void startFaceLogin(ReadableMap configuration, Promise promise) {
    Log.d("INC", "Starting faceLogin");
    SelfieScan selfieScan = new SelfieScan.Builder()
      .setCustomerToken(configuration.getString("customerToken"))
      .setShowTutorials(configuration.getBoolean("showTutorials"))
      .setMode(SelfieScan.Mode.LOGIN)
      .setFaceRecognitionMode(SelfieScan.FaceRecognitionMode.SERVER)
      .setLivenessDetectionMode(SelfieScan.LivenessDetectionMode.SERVER)
      .build();

    IncodeWelcome.getInstance().startSelfieScan(getCurrentActivity(), null, selfieScan, new SelfieScanListener() {
      @Override
      public void onSelfieScanCompleted(SelfieScanResult selfieScanResult) {
        WritableMap map = Arguments.createMap();
        map.putBoolean("faceMatched", selfieScanResult.isFaceMatched());
        map.putBoolean("spoofAttempt", selfieScanResult.isSpoofAttempt());
        promise.resolve(map);
      }

      @Override
      public void onError(Throwable throwable) {
        promise.reject(throwable);
      }

      @Override
      public void onUserCancelled() {
        userCancelled(promise, "Incd::APPROVE::UserCancelled");
      }
    });

  }

  private void userCancelled(Promise promise, String reasonCode) {
    promise.reject(reasonCode, "User cancelled");
  }

  @ReactMethod
  public void getUserScore(ReadableMap configuration, Promise promise) {
    IDResultsFetchMode mode = "fast".equals(configuration.getString("mode")) ? IDResultsFetchMode.FAST : IDResultsFetchMode.ACCURATE;
    Log.d("INCD", "GetUserScore " + mode.name());
    IncodeWelcome.getInstance().getUserScore(mode, null, new GetUserScoreListener() {
      @Override
      public void onUserScoreFetched(UserScoreResult userScoreResult) {
        promise.resolve(mapUserScoreResult(userScoreResult));
      }

      @Override
      public void onError(Throwable throwable) {
        promise.reject(throwable);
      }

      @Override
      public void onUserCancelled() {
        userCancelled(promise, "Incd::GET_USER_SCORE::UserCancelled");
      }
    });
  }

  public static ReadableMap mapUserScoreResult(UserScoreResult userScoreResult) {
    return new UserScoreMapper().map(userScoreResult);
  }

}
