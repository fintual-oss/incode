package com.reactnativeincodesdk;

public class UnknownModuleException extends IllegalArgumentException {
  public UnknownModuleException(String moduleName) {
    super("Unknown module " + moduleName + " specified in Onboarding configuration");
  }
}
