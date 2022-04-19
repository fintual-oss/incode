package com.reactnativeincodesdk;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.incode.welcome_sdk.IncodeWelcome;
import com.incode.welcome_sdk.data.remote.beans.ResponseMedicalDoc;
import com.incode.welcome_sdk.results.ApproveResult;
import com.incode.welcome_sdk.results.CaptchaResult;
import com.incode.welcome_sdk.results.DocumentValidationResult;
import com.incode.welcome_sdk.results.FaceMatchResult;
import com.incode.welcome_sdk.results.GeolocationResult;
import com.incode.welcome_sdk.results.IdValidationResult;
import com.incode.welcome_sdk.results.PhoneNumberResult;
import com.incode.welcome_sdk.results.QRScanResult;
import com.incode.welcome_sdk.results.SelfieScanResult;
import com.incode.welcome_sdk.results.SignatureFormResult;
import com.incode.welcome_sdk.results.UserConsentResult;
import com.incode.welcome_sdk.results.UserScoreResult;
import com.incode.welcome_sdk.ui.camera.id_validation.base.DocumentType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.reactnativeincodesdk.ModuleNames.APPROVE;
import static com.reactnativeincodesdk.ModuleNames.CAPTCHA;
import static com.reactnativeincodesdk.ModuleNames.CONFERENCE;
import static com.reactnativeincodesdk.ModuleNames.CONSENT;
import static com.reactnativeincodesdk.ModuleNames.DOCUMENT_SCAN;
import static com.reactnativeincodesdk.ModuleNames.FACE_MATCH;
import static com.reactnativeincodesdk.ModuleNames.GEOLOCATION;
import static com.reactnativeincodesdk.ModuleNames.ID_SCAN;
import static com.reactnativeincodesdk.ModuleNames.PHONE;
import static com.reactnativeincodesdk.ModuleNames.QR_SCAN;
import static com.reactnativeincodesdk.ModuleNames.SELFIE_SCAN;
import static com.reactnativeincodesdk.ModuleNames.SIGNATURE;
import static com.reactnativeincodesdk.ModuleNames.USER_SCORE;

class ReactOnboardingListener extends IncodeWelcome.OnboardingListenerV2 {
  private static final double SPOOF_THRESHOLD = 0.0;
  private static final double FACE_MATCH_THRESHOLD = 0.0;
  private final Promise promise;
  private final ReactApplicationContext reactApplicationContext;

  public ReactOnboardingListener(Promise promise, ReactApplicationContext reactApplicationContext) {
    this.promise = promise;
    this.reactApplicationContext = reactApplicationContext;
  }

  private static WritableMap addResultsToEventData(String stepName, StepResultsMapping mapping) {
    WritableMap map = Arguments.createMap();
    map.putString("module", stepName);
    WritableMap results = Arguments.createMap();
    mapping.run(results);
    map.putMap("result", results);
    return map;
  }

  private static void addMapToResults(WritableMap map, String atPath, StepResultsMapping mapping) {
    WritableMap results = Arguments.createMap();
    mapping.run(results);
    map.putMap(atPath, results);
  }

  @Override
  public void onOnboardingSessionCreated(String token, String interviewId, String region) {
    WritableMap map = Arguments.createMap();
    map.putString("interviewId", interviewId);
    emit(map, "ONBOARDING_SESSION_CREATED");
    super.onOnboardingSessionCreated(token, interviewId, region);
  }

  private void emit(WritableMap map, String type) {
    reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(type, map);
  }

  @Override
  public void onAddPhoneCompleted(PhoneNumberResult phoneNumberResult) {
    emitOnboardingStepCompleted(addResultsToEventData(PHONE, results -> {
      results.putString("phone", phoneNumberResult.getPhone());
      results.putString("resultCode", phoneNumberResult.getResultCode().name());
    }));

    super.onAddPhoneCompleted(phoneNumberResult);
  }

  @Override
  public void onApproveCompleted(ApproveResult approveResult) {
    boolean success = approveResult.getUuid() != null;
    emitOnboardingStepCompleted(addResultsToEventData(APPROVE, results -> {
      results.putString("approved", success ? "approved" : "failed");
      results.putString("id", approveResult.getUuid());
      results.putString("customerToken", approveResult.getToken());
    }));
    super.onApproveCompleted(approveResult);
  }

  @Override
  public void onGovernmentValidationCompleted(boolean success) {
    super.onGovernmentValidationCompleted(success);
  }

  @Override
  public void onResultsShown(UserScoreResult userScoreResult) {
    WritableMap map = Arguments.createMap();
    map.putString("module", USER_SCORE);
    map.putMap("result", new UserScoreMapper().map(userScoreResult));
    emitOnboardingStepCompleted(map);
    super.onResultsShown(userScoreResult);
  }

  @Override
  public void onCaptchaCollected(CaptchaResult captchaResponse) {
    emitOnboardingStepCompleted(addResultsToEventData(CAPTCHA, results -> {
      results.putString("response", captchaResponse.getCaptchaResponse());
      results.putString("status", captchaResponse.getResultCode().name().toLowerCase());
    }));
    super.onCaptchaCollected(captchaResponse);
  }

  private static ReadableMap image(String frontIdPath) {
    if (TextUtils.isEmpty(frontIdPath)) {
      return Arguments.createMap();
    }
    try {
      FileInputStream fileInputStream = new FileInputStream(frontIdPath);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] bytes = new byte[8192];
      int bytesRead = -1;

      while ((bytesRead = fileInputStream.read(bytes)) != -1) {
        byteArrayOutputStream.write(bytes, 0, bytesRead);
      }
      WritableMap ret = Arguments.createMap();

      ret.putString("pngBase64",
        Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
      return ret;
    } catch (FileNotFoundException e) {
      Log.e("INCD", "images packing error: ", e);
    } catch (IOException e) {
      Log.e("INCD", "images packing error: ", e);
    }
    return Arguments.createMap();
  }

  @Override
  public void onIdValidationCompleted(IdValidationResult idValidationResult) {
    Log.d("INCD", "onIdValidationCompleted: " + idValidationResult);
    int backIdResult = idValidationResult.getBackIdResult();
    int frontIdResult = idValidationResult.getFrontIdResult();

    StepResultsMapping putIdValidationStatus = results -> {
      WritableMap status = Arguments.createMap();
      status.putString("front", IdValidationResultMapping.fromCode(frontIdResult));
      status.putString("back", IdValidationResultMapping.fromCode(backIdResult));
      results.putMap("status", status);

      if (idValidationResult.getOcrData() != null) {
        Map<String, Object> extendedOcrJsonData = idValidationResult.getOcrData().getExtendedOcrJsonData();

        if (extendedOcrJsonData.get("extendedData") != null) {
          try {
            String dummyDataJSON = new JSONObject("{\"ocrData\":{\"curp\":\"MXMO860324HDFRDM05\",\"cic\":\"153654132\",\"registrationDate\":2004,\"numeroEmisionCredencial\":\"01\",\"birthDate\":512006400000,\"issueDate\":2012,\"claveDeElector\":\"MRMDOM86032409H800\",\"addressFields\":{\"colony\":\"COL JORGE NEGRETE\",\"street\":\"C MARIO MORENO MZ 201E LT 4\",\"city\":\"GUSTAVO A. MADERO\",\"state\":\"D.F.\",\"postalCode\":\"07280\"},\"fullNameMrz\":\"Omar Martinez Madrid\",\"expirationDate\":2022,\"address\":\"C MARIO MORENO MZ 201E LT 4 COL JORGE NEGRETE 07280 GUSTAVO A. MADERO ,D.F.\",\"ocr\":\"3145897456321\",\"gender\":\"M\",\"name\":{\"firstName\":\"Omar\",\"paternalLastName\":\"Martinez\",\"fullName\":\"Omar Martinez Madrid\",\"maternalLastName\":\"Madrid\"}}}").toString();
            results.putString("extendedOcrData", dummyDataJSON);
          } catch (JSONException e) {
            Log.w("INCD", "Couldn't parse extendedOCR: " + e.getMessage());
          }
        } else {
          Map<String, Object> wrappedExtendedOcrJsonData = new HashMap<String, Object>() {{
            put("ocrData", extendedOcrJsonData);
          }};
          String ocrDataString = new JSONObject(wrappedExtendedOcrJsonData).toString();
          results.putString("extendedOcrData", ocrDataString);

        }
      }

      WritableMap images = Arguments.createMap();
      images.putMap("front", image(idValidationResult.getFrontIdPath()));
      images.putMap("back", image(idValidationResult.getBackIdPath()));

      results.putMap("images", images);
    };


    emitOnboardingStepCompleted(
      addResultsToEventData(ID_SCAN, results -> {
        addMapToResults(results, "data", ocrResultsMap -> {
          if (idValidationResult.getOcrData() != null) {
            putOcrResult(ocrResultsMap, idValidationResult.getOcrData());
          }
        });
        putIdValidationStatus.run(results);
      })
    );
    super.onIdValidationCompleted(idValidationResult);
  }

  @Override
  public void onDocumentValidationCompleted(DocumentType documentType, DocumentValidationResult documentValidationResult) {
    emitOnboardingStepCompleted(addResultsToEventData(DOCUMENT_SCAN, results -> {
      results.putString("status", "success");
      results.putMap("image", image(documentValidationResult.getDocumentPath()));
      IncodeWelcome.AddressFields addressFields = documentValidationResult.getAddressFields();

      WritableMap address = Arguments.createMap();
      addAllAddressFields(address, addressFields);
      results.putMap("address", address);

//      addMapToResults(results, "insuranceCard", insuranceResults -> {
//        ResponseMedicalDoc insuranceCardData = documentValidationResult.getMedicalDocData();
//        if (insuranceCardData != null) {
//          insuranceResults.putString("copayEr", insuranceCardData.getCopayEr());
//          insuranceResults.putString("copayOv", insuranceCardData.getCopayOv());
//          insuranceResults.putString("member", insuranceCardData.getMember());
//          insuranceResults.putString("memberId", insuranceCardData.getMemberId());
//          insuranceResults.putString("provider", insuranceCardData.getProvider());
//          insuranceResults.putString("rxBin", insuranceCardData.getRxBin());
//          insuranceResults.putString("rxPcn", insuranceCardData.getRxPcn());
//          insuranceResults.putString("rawData", insuranceCardData.getRawData());
//        }
//      });
    }));
    super.onDocumentValidationCompleted(documentType, documentValidationResult);
  }


  @Override
  public void onGeolocationFetched(GeolocationResult geolocationResult) {
    emitOnboardingStepCompleted(addResultsToEventData(GEOLOCATION, results -> {
      addAllAddressFields(results, geolocationResult.getAddressFields());
    }));
    super.onGeolocationFetched(geolocationResult);
  }

  @Override
  public void onSelfieScanCompleted(SelfieScanResult selfieScanResult) {
    if (selfieScanResult == null) {
      return;
    }
    emitOnboardingStepCompleted(addResultsToEventData(SELFIE_SCAN, results -> {
      Log.d("INCD", "selfieScanResult: " + selfieScanResult);
      boolean spoofAttempt = selfieScanResult.isSpoofAttempt() != null && selfieScanResult.isSpoofAttempt() == true;
      if (selfieScanResult.isSpoofAttempt() != null) {
        results.putBoolean("spoofAttempt", selfieScanResult.isSpoofAttempt());
      }

      if (selfieScanResult.isSpoofAttempt() == null) {
        results.putString("status", "unknown");
      } else {
        results.putString("status", spoofAttempt ? "spoof" : "success");
      }
      results.putMap("image", image(selfieScanResult.getFullFrameSelfieImgPath()));
    }));
    super.onSelfieScanCompleted(selfieScanResult);
  }

  @Override
  public void onFaceMatchCompleted(FaceMatchResult faceMatchResult) {
    emitOnboardingStepCompleted(addResultsToEventData(FACE_MATCH, results -> {
      results.putString("status", faceMatchResult.getConfidence() > FACE_MATCH_THRESHOLD ? "match" : "mismatch");
    }));
    super.onFaceMatchCompleted(faceMatchResult);
  }

  @Override
  public void onSignatureCollected(SignatureFormResult signatureFormResult) {
    emitOnboardingStepCompleted(addResultsToEventData(SIGNATURE, results -> {
      results.putString("status", "success");
    }));
    super.onSignatureCollected(signatureFormResult);
  }

  @Override
  public void onQueuePositionChanged(int newQueuePosition) {
    emit(addResultsToEventData(CONFERENCE, result -> {
      result.putInt("newQueuePosition", newQueuePosition);
    }), "ONBOARDING_STEP_UPDATE");
    super.onQueuePositionChanged(newQueuePosition);
  }

  @Override
  public void onQRScanCompleted(QRScanResult qrScanResult) {
    emit(addResultsToEventData(QR_SCAN, result -> {
      result.putString("idCic", qrScanResult.getIdCic());
    }), "ONBOARDING_STEP_UPDATE");
  }

  @Override
  public void onConferenceEnded() {
    super.onConferenceEnded();
    emitOnboardingStepCompleted(addResultsToEventData(CONFERENCE, writableMap -> {
      writableMap.putString("status", "success");
    }));
  }

  @Override
  public void onEstimatedWaitingTime(int waitingTimeInSeconds) {
    emit(addResultsToEventData(CONFERENCE, result -> {
      result.putInt("newWaitingTimeSeconds", waitingTimeInSeconds);
    }), "ONBOARDING_STEP_UPDATE");
    super.onEstimatedWaitingTime(waitingTimeInSeconds);
  }

  @Override
  public void onGeolocationUnavailable(Throwable error) {
    super.onGeolocationUnavailable(error);
    emitOnboardingStepError(addResultsToEventData("GEOLOCATION", map -> {
      map.putString("status", "permissionsDenied");
    }));
  }

  @Override
  public void onUserConsentCompleted() {
    emitOnboardingStepCompleted(addResultsToEventData(CONSENT, results -> {
      results.putString("status", "success");
    }));
  }

  @Override
  public void onUserCancelled() {
    resolveWithStatus("userCancelled");
  }

  private void resolveWithStatus(final String status) {
    WritableMap writableMap = Arguments.createMap();
    writableMap.putString("status", status);
    promise.resolve(writableMap);
  }

  @Override
  public void onSuccess() {
    super.onSuccess();
    Log.d("INCD::ONBOARDING", "Success");
    resolveWithStatus("success");
  }

  @Override
  public void onError(Throwable throwable) {
    super.onError(throwable);
    Log.d("INCD::ONBOARDING", "ERROR", throwable);
    promise.reject("Incd::ONBOARDING_ERROR", throwable);
  }

  @Override
  public void onOnboardingSectionCompleted(String flowTag) {
    super.onOnboardingSectionCompleted(flowTag);
    Log.d("INCD::ONBOARDING", "Section complete");
    resolveWithStatus("success");
  }

  private void putOcrResult(WritableMap results, IncodeWelcome.OCRData ocrData) {
    results.putString("fullAddress", ocrData.getAddress());
    addMapToResults(results, "address", map -> addAllAddressFields(map, ocrData.getAddressFields()));
    results.putString("birthDate", ocrData.getBirthDate());
    results.putInt("expirationDate", ocrData.getExpirationDate());
    results.putString("gender", ocrData.getGender());
    results.putString("name", ocrData.getFullName());
    results.putInt("issueDate", ocrData.getIssueDate());
    results.putString("numeroEmisionCredencial", ocrData.getNumeroEmisionCredencial());
  }

  private void addAllAddressFields(WritableMap resultsMap, IncodeWelcome.AddressFields addressFields) {
    if (addressFields != null) {
      resultsMap.putString("city", addressFields.getCity());
      resultsMap.putString("colony", addressFields.getColony());
      resultsMap.putString("postalCode", addressFields.getPostalCode());
      resultsMap.putString("state", addressFields.getState());
      resultsMap.putString("street", addressFields.getStreet());
    }
  }

  private void emitOnboardingStepCompleted(WritableMap map) {
    emit(map, "ONBOARDING_STEP_COMPLETED");
  }

  private void emitOnboardingStepError(WritableMap map) {
    emit(map, "ONBOARDING_STEP_ERROR");
  }

  interface StepResultsMapping {
    void run(WritableMap writableMap);
  }


  static class IdValidationResultMapping {
    static HashMap<Integer, String> validationResultMap = new HashMap<Integer, String>() {
      {
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_USER_CANCELLED, "userCancelled");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_UNKNOWN, "unknownError");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_OK, "ok");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_CLASSIFICATION, "errorClassification");
        put(com.incode.welcome_sdk.results.IdValidationResult.FRONT_ID_RESULT_ERROR_NO_FACES_FOUND, "noFacesFound");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_GLARE, "errorGlare");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_SHARPNESS, "errorSharpness");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_SHADOW, "errorShadow");
        put(com.incode.welcome_sdk.results.IdValidationResult.PASSPORT_RESULT_ERROR_CLASSIFICATION, "errorPassportClassification");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_ADDRESS_STATEMENT, "errorAddress");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_ERROR_READABILITY, "errorReadability");
        put(com.incode.welcome_sdk.results.IdValidationResult.RESULT_EMULATOR_DETECTED, "emulatorDetected");
      }
    };

    public static String fromCode(int idValidationResult) {
      return validationResultMap.get(idValidationResult);
    }
  }
}
