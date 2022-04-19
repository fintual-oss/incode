package com.reactnativeincodesdk;

import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.util.JsonUtils;
import com.incode.welcome_sdk.IncodeWelcome.IDResultsFetchMode;
import com.incode.welcome_sdk.OnboardingConfigV2;
import com.incode.welcome_sdk.OnboardingConfigV2.OnboardingConfigBuilderV2;
import com.incode.welcome_sdk.modules.DocumentScan;
import com.incode.welcome_sdk.modules.IdScan;
import com.incode.welcome_sdk.modules.SelfieScan;
import com.incode.welcome_sdk.modules.UserConsent;
import com.incode.welcome_sdk.modules.VideoSelfie;
import com.incode.welcome_sdk.modules.exceptions.ModuleConfigurationException;
import com.incode.welcome_sdk.ui.camera.id_validation.base.DocumentType;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;


public class OnboardingConfigFactory {
  public static final boolean SHOW_TUTORIAL_DEFAULT = true;
  private final OnboardingConfigV2 config;

  protected OnboardingConfigFactory(OnboardingConfigV2 config) {
    this.config = config;
  }

  public static OnboardingConfigFactory NEW_FROM(ReadableArray modulesConfig, String interviewId, String configurationId, UserPreferences userPreferences) throws ModuleConfigurationException {

    OnboardingConfigBuilderV2 builderV2 = buildFromConfig(modulesConfig, userPreferences);
    builderV2.setInterviewId(interviewId);
    builderV2.setConfigurationId(configurationId);
    Log.i("IncSDK", "Onboarding configured ✅");
    return new OnboardingConfigFactory(builderV2.build());

  }

  @NotNull
  private static OnboardingConfigBuilderV2 buildFromConfig(ReadableArray modulesConfig, UserPreferences userPreferences) {
    Set<String> includedModules = new LinkedHashSet<>();
    OnboardingConfigBuilderV2 builderV2 = new OnboardingConfigBuilderV2();

//    builderV2.setQueueName(Constants.Prefs.Defaults.QUEUE_NAME_DEFAULT.equals(queueName) ? null : queueName) //TODO @nenad set queue name

    for (int i = 0; i < modulesConfig.size(); i++) {
      ReadableMap module = modulesConfig.getMap(i);
      if (module == null) {
        continue;
      }
      String moduleName = module.getString("module");

      if (moduleName == null) {
        throw new IllegalArgumentException("Module name not present in config");
      }
      if (includedModules.contains(moduleName)) {
        continue;
      }

      boolean isEnabled = module.getBoolean("enabled");
      if (!isEnabled) {
        continue;
      }

      boolean showTutorial = SHOW_TUTORIAL_DEFAULT;
      if (module.hasKey("showTutorial")) {
        showTutorial = module.getBoolean("showTutorial");
      }
      String queueName = userPreferences.getQueueName();
      Log.d("INCD", "Configuring module" + moduleName);

      switch (moduleName) {
        case ModuleNames.SELFIE_SCAN:
          Log.d("INCD", "**SELFIE SCAN");
          SelfieScan.Mode selfieScanMode = "login".equals(module.getString("mode")) ? SelfieScan.Mode.LOGIN : SelfieScan.Mode.ENROLL;
          String customerToken = module.getString("customerToken");

          SelfieScan.Builder builder = new SelfieScan.Builder()
            .setShowTutorials(showTutorial)
            .setLivenessDetectionMode(SelfieScan.LivenessDetectionMode.SERVER)
            .setMode(selfieScanMode)
            .setCustomerToken(customerToken);

          if (selfieScanMode == SelfieScan.Mode.LOGIN) {
            builder.setFaceRecognitionMode(SelfieScan.FaceRecognitionMode.SERVER);
            ReadableMap loginOptions = module.getMap("loginOptions");
            if (loginOptions != null) {
              if (loginOptions.hasKey("faceRecognitionThreshold")) {
                float recognitionThreshold = (float) loginOptions.getDouble("faceRecognitionThreshold");
                Log.d("INCD SELFIE", "buildFromConfig: " + recognitionThreshold);
                builderV2.setRecognitionThreshold(recognitionThreshold);
              }
              if (loginOptions.hasKey("spoofThreshold")) {
                float spoofThreshold = (float) loginOptions.getDouble("spoofThreshold");
                Log.d("INCD SELFIE", "buildFromConfig: " + spoofThreshold);
                builderV2.setSpoofThreshold(spoofThreshold);
              }
            }
          }

          builderV2.addSelfieScan(builder.build());
          break;
        case ModuleNames.ID_SCAN:
          Log.d("INCD", "**ID SCAN");
          String idType = module.getString("idType");
          builderV2.addIDScan(
            new IdScan.Builder()
              .setShowIdTutorials(showTutorial)
              .setIdType(idType != null ? IdScan.IdType.valueOf(idType.toUpperCase()) : null)
              .build()
          );
          break;
        case ModuleNames.CONFERENCE:
          Log.d("INCD", "**Conference");
          if (!userPreferences.getConferenceMode().isConferenceEnabled()) {
            break;
          }
          builderV2.addConference(module.getBoolean("disableMicOnStart"));
          break;
        case ModuleNames.DOCUMENT_SCAN:
          Log.d("INCD", "**Document Scan");

          DocumentScan.Builder docScanBuilder = new DocumentScan.Builder()
            .setShowTutorials(showTutorial)
            .setDocumentType(DocumentType.ADDRESS_STATEMENT);

          if (module.hasKey("showDocumentProviderScreen")) {
            docScanBuilder.setShowDocumentProviderOptions(module.getBoolean("showDocumentProviderScreen"));
          }

          builderV2.addDocumentScan(docScanBuilder.build());
          break;
        case ModuleNames.GEOLOCATION:
          Log.d("INCD", "**Geolocation");
          builderV2.addGeolocation();
          break;
        case ModuleNames.FACE_MATCH:
          Log.d("INCD", "**Face match");
          builderV2.addFaceMatch();
          break;
        case ModuleNames.PHONE:
          Log.d("INCD", "**PHONE");
          builderV2.addPhone();
          break;
        case ModuleNames.SIGNATURE:
          Log.d("INCD", "**Signature");
          builderV2.addSignature();
          break;
        case ModuleNames.QR_SCAN:
          Log.d("INCD", "**QR_SCAN");
          builderV2.addQRScan(showTutorial);
          break;
        case ModuleNames.USER_SCORE:
          Log.d("INCD", "**Results");
          IDResultsFetchMode mode = "fast".equals(module.getString("mode")) ? IDResultsFetchMode.FAST : IDResultsFetchMode.ACCURATE;
          builderV2.addResults(mode);
          break;
        case ModuleNames.APPROVE:
          Log.d("INCD", "**APPROVE");
          builderV2.addApproval(false, false, module.getBoolean("forceApproval"));
          break;
        case ModuleNames.VIDEO_SELFIE:
          Log.d("INCD", "**Video selfie");
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VideoSelfie.Builder vsBuilder = new VideoSelfie.Builder();
            if (module.hasKey("showTutorial")) {
              vsBuilder.setShowTutorials(showTutorial);
            }

            if (module.hasKey("selfieLivenessCheck")) {
              vsBuilder.setLivenessEnabled(module.getBoolean("selfieLivenessCheck"));
            }

            if (module.hasKey("selfieScanMode")) {
              vsBuilder.setSelfieMode("faceMatch".equals(module.getString("selfieScanMode")) ? VideoSelfie.SelfieMode.FACE_MATCH : VideoSelfie.SelfieMode.SELFIE_MATCH);
            }

            if (module.hasKey("showIdScan")) {
              vsBuilder.setIdScanEnabled(module.getBoolean("showIdScan"));
            }

            if (module.hasKey("showDocumentScan")) {
              vsBuilder.setDocumentScanEnabled(module.getBoolean("showDocumentScan"));
            }

            if (module.hasKey("showVoiceConsent")) {
              vsBuilder.setVoiceConsentEnabled(module.getBoolean("showVoiceConsent"));
              vsBuilder.setRandomQuestionsEnabled(module.getBoolean("showVoiceConsent"));
            }

            if (module.hasKey("voiceConsentQuestionsCount")) {
              vsBuilder.setRandomQuestionsCount(module.getInt("voiceConsentQuestionsCount"));
            }

            builderV2.addVideoSelfie(vsBuilder.build());
          } else {
            Log.w("INCD", "Video selfie module not available for SDK version " + Build.VERSION.SDK_INT);
          }
          // TODO: what to do otherwise
          break;
        case ModuleNames.CAPTCHA:
          builderV2.addCaptcha();
          break;
        case ModuleNames.CONSENT:
          String title = module.getString("title");
          String content = module.getString("content");
          builderV2.addUserConsent(new UserConsent.Builder().setTitle(title).setContent(content).build());
          break;
        case ModuleNames.GOVERNMENT_VALIDATION:
          builderV2.addGovernmentValidation();
        default:
          Log.d("INCD", "**Wrong " + moduleName);
          throw new UnknownModuleException(moduleName);
      }
      includedModules.add(moduleName);
    }
    return builderV2;
  }

  public OnboardingConfigV2 getConfig() {
    return config;
  }
}
