package com.reactnativeincodesdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.incode.welcome_sdk.IncodeWelcome;
import com.incode.welcome_sdk.commons.PublicConstants;
import com.incode.welcome_sdk.data.local.RegionV2;
import com.reactnativeincodesdk.Constants.Prefs;
import com.reactnativeincodesdk.Constants.Prefs.Defaults;

public class UserPreferences {

  private SharedPreferences userPreferences;

  public UserPreferences(Context context) {
    userPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
  }

  public void setClientId(String clientId) {
    userPreferences.edit().putString(Prefs.CLIENT_ID, clientId).apply();
  }

  public String getClientId() {
    return userPreferences.getString(Prefs.CLIENT_ID, "");
  }

  public void setAPIKey(String apiKey) {
    userPreferences.edit().putString(Prefs.API_KEY, apiKey).apply();
  }

  public String getAPIKey() {
    return userPreferences.getString(Prefs.API_KEY, "");
  }

  public void setIdAutoCaptureTimeout(int idAutoCaptureTimeout) {
    userPreferences.edit().putInt(Prefs.ID_AUTO_CAPTURE_TIMEOUT, idAutoCaptureTimeout).apply();
  }

  public int getIdAutoCaptureTimeout() {
    return userPreferences.getInt(Prefs.ID_AUTO_CAPTURE_TIMEOUT, Defaults.ID_AUTO_CAPTURE_TIMEOUT);
  }

  public void setIdResultsMode(int idResultsMode) {
    userPreferences.edit().putInt(Prefs.ID_RESULTS_MODE, idResultsMode).apply();
  }

  public int getIdResultsMode() {
    return userPreferences.getInt(Prefs.ID_RESULTS_MODE, Defaults.ID_RESULTS_FETCH_MODE_DEFAULT.ordinal());
  }

  public void setQueueName(String queueName) {
    userPreferences.edit().putString(Prefs.QUEUE_NAME, queueName).apply();
  }

  public String getQueueName() {
    return userPreferences.getString(Prefs.QUEUE_NAME, Defaults.QUEUE_NAME_DEFAULT);
  }

  public boolean isSkipVerifyAddress() {
    return userPreferences.getBoolean(Prefs.SKIP_ADDRESS_VALIDATION, Defaults.SKIP_ADDRESS_VALIDATION_DEFAULT);
  }

  public void setSkipVerifyAddress(boolean skipVerifyAddress) {
    userPreferences.edit().putBoolean(Prefs.SKIP_ADDRESS_VALIDATION, skipVerifyAddress).apply();
  }

  public ConferenceMode getConferenceMode() {
    ConferenceMode result = Defaults.CONFERENCE_OR_RESULTS_DEFAULT;
    String conferenceOrResultsEnumName = userPreferences.getString(Prefs.CONFERENCE_OR_RESULTS, null);
    if (conferenceOrResultsEnumName != null) {
      result = ConferenceMode.valueOf(conferenceOrResultsEnumName);
    }
    return result;
  }

  public void setConferenceOrResults(ConferenceMode conferenceOrResults) {
    if (conferenceOrResults == null) {
      return;
    }
    userPreferences.edit().putString(Prefs.CONFERENCE_OR_RESULTS, conferenceOrResults.name()).apply();
  }

  public RegionV2 getRegion() {
    return new RegionV2(
      userPreferences.getString(Prefs.REGION_LABEL, PublicConstants.DEFAULT_REGION_LABEL),
      userPreferences.getString(Prefs.REGION_ISO_CODE, PublicConstants.DEFAULT_REGION_ISO_CODE),
      userPreferences.getString(Prefs.REGION_EMOJI, null)
    );
  }

  public void setRegion(RegionV2 regionV2) {
    userPreferences.edit()
      .putString(Prefs.REGION_ISO_CODE, regionV2.getCode())
      .putString(Prefs.REGION_LABEL, regionV2.getLabel())
      .putString(Prefs.REGION_EMOJI, regionV2.getEmoji())
      .apply();
  }

  public SdkMode getSdkMode() {
    SdkMode result = Defaults.SDK_MODE_DEFAULT;
    String sdkModeName = userPreferences.getString(Prefs.SDK_MODE, null);
    if (sdkModeName != null) {
      result = SdkMode.valueOf(sdkModeName);
    }
    return result;
  }

  public void setSdkMode(SdkMode sdkMode) {
    if (sdkMode == null) {
      return;
    }
    userPreferences.edit().putString(Prefs.SDK_MODE, sdkMode.name()).apply();
  }

  public IncodeWelcome.IDResultsFetchMode getIdResultsFetchMode() {
    switch (getIdResultsMode()) {
      case 0:
        return IncodeWelcome.IDResultsFetchMode.ACCURATE;
      case 1:
        return IncodeWelcome.IDResultsFetchMode.FAST;
      default:
        return IncodeWelcome.IDResultsFetchMode.ACCURATE;
    }
  }
}
