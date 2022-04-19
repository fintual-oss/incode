package com.reactnativeincodesdk;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.incode.welcome_sdk.data.remote.beans.FacialRecognitionResults;
import com.incode.welcome_sdk.results.UserScoreResult;

public class UserScoreMapper {
  public ReadableMap map(UserScoreResult userScoreResult) {
    WritableMap map = Arguments.createMap();
    map.putString("overallScore", userScoreResult.getOverallScore());
    map.putString("status", userScoreResult.getOverallStatus().name().toLowerCase());
    FacialRecognitionResults facialRecognitionResults = userScoreResult.getFacialRecognitionResults();
    map.putString("facialRecognitionScore", facialRecognitionResults.getOverallScore());
    map.putBoolean("existingUser", facialRecognitionResults.isExistingUser());
    map.putString("idVerificationScore", userScoreResult.getIdVerificationResults().getOverallScore());
    map.putString("livenessOverallScore", userScoreResult.getLivenessCheckResults().getOverallScore());
    return map;
  }
}
