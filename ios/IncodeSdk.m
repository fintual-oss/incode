#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(IncodeSdk, RCTEventEmitter)

RCT_EXTERN_METHOD(initialize:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startOnboarding:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(createOnboardingSession:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startOnboardingSection:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(finishOnboardingFlow: (RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getUserScore:(NSDictionary *)config
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(approve:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                    withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startFaceLogin:(NSDictionary *)config
                    withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(supportedEvents)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}
@end
