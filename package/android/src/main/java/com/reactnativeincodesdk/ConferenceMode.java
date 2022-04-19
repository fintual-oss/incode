package com.reactnativeincodesdk;

public enum ConferenceMode {
  CONFERENCE,
  RESULTS,
  BOTH;

  public boolean isConferenceEnabled() {
    return this == CONFERENCE || this == BOTH;
  }

  public boolean isResultsScreenEnabled() {
    return this == RESULTS || this == BOTH;
  }
}
