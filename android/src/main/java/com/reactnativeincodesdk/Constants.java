package com.reactnativeincodesdk;

import com.incode.welcome_sdk.IncodeWelcome;

public class Constants {
  public static final String TERMS_PRIVACY_URL = "http://incode.com/terms-of-service.html";
  public static final String PRIVACY_POLICY_URL = "http://incode.com/privacy-policy.html";

  public static class Prefs {
    public static final String APPLICATION_EXISTS = "prefsIsFaceEnrolled";
    public static final String APPLICANT_NAME = "prefsOnboardingApplicantName";
    public static final String ID_AUTO_CAPTURE_TIMEOUT = "prefsIdAutoCaptureTimeout";
    public static final String QUEUE_NAME = "prefsQueueName";
    public static final String CONFERENCE_OR_RESULTS = "prefsConferenceOrResults";
    public static final String FULL_ID_SCAN = "prefsFullIdScan";
    public static final String SKIP_ADDRESS_VALIDATION = "prefsSkipAddressValidation";
    public static final String API_KEY = "prefsAPIKey";
    public static final String CLIENT_ID = "prefsClientId";
    public static final String ID_RESULTS_MODE = "prefsIdResultsMode";
    public static final String REGION_ISO_CODE = "prefsRegionIsoCode";
    public static final String REGION_LABEL = "prefsRegionLabel";
    public static final String REGION_EMOJI = "prefsRegionEmoji";
    public static final String SDK_MODE = "prefsSDKMode";

    public static class Defaults {
      public static final double SPOOF_DETECTION_THRESHOLD = 0.6;
      public static final double FACE_RECOGNITION_THRESHOLD = 0.3;
      public static final int ID_AUTO_CAPTURE_TIMEOUT = 25;
      public static final String QUEUE_NAME_DEFAULT = "default";
      public static final ConferenceMode CONFERENCE_OR_RESULTS_DEFAULT = ConferenceMode.RESULTS;
      public static final boolean FULL_ID_SCAN_DEFAULT = true;
      public static final IncodeWelcome.IDResultsFetchMode ID_RESULTS_FETCH_MODE_DEFAULT = IncodeWelcome.IDResultsFetchMode.ACCURATE;
      public static final boolean SKIP_ADDRESS_VALIDATION_DEFAULT = false;
      public static final SdkMode SDK_MODE_DEFAULT = SdkMode.STANDARD;
    }
  }

}
