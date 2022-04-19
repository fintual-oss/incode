//
//  RCTINCOnboardingDelegate.swift
//  react-native-incode-sdk
//
//  Created by Nenad Vitorovic on 2/17/21.
//

import Foundation
import IncdOnboarding

typealias OcrAddress = OCRDataAddress

class RCTINCOnboardingDelegate: IncdOnboardingDelegate{
  var onboardingSuccess: Bool?
  let resolve:RCTPromiseResolveBlock
  let reject:RCTPromiseRejectBlock
  let incdSdk: UIViewController
  //    let beforeIncWillAppear: ()->Void
  
  init(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock, incdSdk: UIViewController) {
    self.resolve = resolve
    self.reject = reject
    self.incdSdk = incdSdk
    //        self.beforeIncWillAppear = beforeIncWillAppear
  }
  
  //MARK:- IncdOnboardingDelegate
  func onOnboardingSessionCreated(_ result: OnboardingSessionResult) {
    print("onOnboardingSessionCreated: \(result)")
    if let error = result.error {
      self.reject("Incd:ONBOARDING_ERROR", "There was an error in creating onboarding session: \(error.rawValue)", nil)
      return
    }
    
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_SESSION_CREATED", body: ["interviewId": result.interviewId])
    //        self.beforeIncWillAppear()
    
  }
  
  //MARK:- RN promises
  func onError(_ error: IncdFlowError) {
    if error.rawValue != IncdFlowError.userCanceled.rawValue {
      reject("Incd:ONBOARDING_ERROR", "An error occurred during onboarding: \(error.rawValue)", nil  )
      incdSdk.dismiss(animated: true)
    } else {
      userCancelledSession()
    }
  }
  
  func onSuccess() {
    print("Incd::ios::onSuccess")
    DispatchQueue.main.async {
      self.incdSdk.dismiss(animated: true){
        self.completeWithSuccess()
      }
    }
  }
  
  func userCancelledSession() {
    print("Incd::ios::onUserCancelledSession")
    DispatchQueue.main.async {
      self.incdSdk.dismiss(animated: true) {
        self.resolve(["status":"userCancelled"])
      }
    }
  }
  
  func onOnboardingSectionCompleted(_ flowTag: String) {
    print("Incd::ios::onOnboardingSectionCompleted")
    DispatchQueue.main.async {
      self.completeWithSuccess()
      self.incdSdk.dismiss(animated: true)
    }
  }
  
  func completeWithSuccess(){
    self.resolve(["status":"success"])
  }
  
  //MARK:- IncdOnboardingDelegate
  func onVideoSelfieCompleted(_ result: VideoSelfieResult) {
    if let error = result.error {
      dispatchStepEventError(stepName: "VideoSelfie", result: "\(error)")
    } else {
      dispatchStepEventCompleted(stepName: "VideoSelfie", result: ["status": result.success])
    }
  }
  
  func onGovernmentValidationCompleted(_ result: GovernmentValidationResult) {
    dispatchStepEventCompleted(stepName: "GovernmentValidation", result: ["status": result])
  }
  
  func onIdValidationCompleted(_ result: IdValidationResult) {
    func optionalIntToStr(_ maybe: Int?) -> String {
      if let it = maybe {
        return "\(it)"
      }
      return ""
    }
    
    if result.error != nil && result.error?.rawValue != IdValidationError.error(.simulatorDetected).rawValue {
      dispatchStepEventError(stepName: "IdValidation", result: [
        "status": [
          "front": "\(result.frontIdResult)",
          "back": "\((result.backIdResult))"
        ]])
      return
    }
    
    var resultData = [String:Any]()
    if let ocr = result.ocrData {
      let extendedOcrData: String = String(data: result.extendedOcrJsonData!, encoding: String.Encoding.utf8) ?? "{}"
      resultData["extendedOcrData"] = extendedOcrData
      
      resultData["data"] = [
        "address": (ocr.addressFields != nil) ? ocrData(addressFields: ocr.addressFields) : [],
        "fullAddress": (ocr.address ?? ""),
        "birthDate": ocr.birthDate ?? 0,
        "expirationDate": ocr.expirationDate ?? 0,
        "gender": (ocr.gender ?? ""),
        "name": (ocr.name?.fullName ?? ""),
        "issueDate": ocr.issueDate ?? 0,
        "numeroEmisionCredencial": (ocr.numeroEmisionCredencial ?? "") as Any
      ]
    }
    
    let back: UIImage? = result.idCaptureResult?.backIdImage
    let front: UIImage? = result.idCaptureResult?.frontIdImage
    resultData["images"] = [
      "back": ["pngBase64": back?.pngData()?.base64EncodedString() ?? ""],
      "front": ["pngBase64": front?.pngData()?.base64EncodedString() ?? ""]
    ]
    
    resultData["status"] = [
      "front": "\(result.frontIdResult)",
      "back": "\((result.backIdResult))"
    ]
    
    dispatchStepEventCompleted(stepName:"IdScan", result: resultData)
  }
  
  func onEstimatedWaitingTime(_ waitingTimeInSeconds: Int) {
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_UPDATE", body: [
      "module": "Conference",
      "result": ["newWaitingTimeSeconds": waitingTimeInSeconds]
    ])
  }
  
  func onDocumentScanCompleted(_ result: DocumentScanResult) {
    dispatchStepEventCompleted(stepName: "DocumentScan", result: [
      "image": ["pngBase64": result.documentImage?.pngData()?.base64EncodedString() ?? ""],
      //        "insuranceCard": [
      //          "name": result.insuranceCardData?.name
      //        ],
      "address": ocrData(addressFields: result.addressFieldsFromPoa)
    ])
  }
  
  func onGeolocationCompleted(_ result: GeolocationResult) {
    if let error = result.error {
      dispatchStepEventError(stepName: "Geolocation", result: "\(error)")
    }else {
      dispatchStepEventCompleted(stepName: "Geolocation", result: ocrData(addressFields: result.addressFields)
      )
    }
  }
  
  func onSelfieScanCompleted(_ result: SelfieScanResult) {
    if let error = result.error,
       error.rawValue != SelfieScanError.error(.simulatorDetected).rawValue {
      dispatchStepEventError(stepName: "SelfieScan", result: "\(error)")
    } else {
      var ret = [String: Any]()
      
      if let spoofAttempt = result.spoofAttempt {
        ret["status"] = !spoofAttempt ? "success": "unknown"
      }
      
      if let imageData = result.image?.pngData() {
        ret["image"] = ["pngBase64": imageData.base64EncodedString()]
      }
      
      if let spoofAttempt = result.spoofAttempt {
        ret["spoofAttempt"] = spoofAttempt
      }
      dispatchStepEventCompleted(stepName: "SelfieScan", result: ret)
    }
  }
  
  
  
  func onSignatureCollected(_ result: SignatureFormResult) {
    if let error = result.error {
      dispatchStepEventError(stepName: "Signature", result: ["status": "error" + error.rawValue])
    } else {
      dispatchStepEventCompleted(stepName: "Signature", result: [
        "status": "success"
      ])
    }
  }
  
  func onQueuePositionChanged(_ newQueuePosition: Int) {
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_UPDATE", body: [
      "module": "Conference",
      "result": ["newQueuePosition": newQueuePosition]
    ])
  }
  
  func onVideoConferenceCompleted(_ success: Bool, _ error: VideoConferenceError?) {
    if let error = error {
      dispatchStepEventError(stepName: "Conference", result: ["status": "error" + error.rawValue])
    } else {
      dispatchStepEventCompleted(stepName: "Conference", result: ["status": "\(success)"])
    }
  }
  
  func onFaceMatchCompleted(_ result: FaceMatchResult) {
    if let error = result.error,
       error.rawValue != IncdError.simulatorDetected.rawValue {
      dispatchStepEventError(stepName: "FaceMatch", result: "\(error)")
    }else{
      dispatchStepEventCompleted(stepName: "FaceMatch", result: ["status": (result.faceMatched ?? false) ? "match" : "mismatch"])
    }
  }
  
  func onApproveCompleted(_ result: ApprovalResult) {
    dispatchStepEventCompleted(stepName: "Approve", result:[
      "status": result.success ? "approved" : "failed",
      "id": result.uuid ?? "",
      "customerToken": result.customerToken ?? ""
    ])
    
  }
  
  func onUserScoreFetched(_ result: UserScore) {
    print("***User Score")
    
    if result.overall != nil {
      dispatchStepEventCompleted(stepName: "UserScore", result: userScoreAsDict(result))
    }else {
      dispatchStepEventCompleted(stepName: "UserScore", result: [
        "debug": "\(result.overall.debugDescription)"
      ])
    }
  }
  
  func onQRScanCompleted(_ result: QRScanResult) {
    if let error = result.error {
      // TOODO: not in android?
      dispatchStepEventError(stepName: "QrScan", result: "\(error.rawValue)")
    }else{
      dispatchStepEventCompleted(stepName: "QrScanCompleted", result: ["idCic": result.idCic ?? ""])
    }
  }
  
  func onAddPhoneNumberCompleted(_ result: PhoneNumberResult) {
    if let error = result.error {
      dispatchStepEventError(stepName: "Phone", result: "\(error.rawValue)")
    }else {
      
      dispatchStepEventCompleted(stepName: "Phone", result: [
        "phone": result.phone ?? ""])
    }
  }
  
  func onUserConsentGiven(_ result: UserConsentResult) {
    print("onUserConsentGiven called, result: \(result)")
    dispatchStepEventCompleted(stepName: "UserConsent", result: [
      "status": result.success == true ? "success" : "fail"
    ])
  }
  
  func onCaptchaCompleted(_ result: CaptchaResult){
    if let error = result.error {
      reject("Incd::ONBOARDING_ERROR", "Captcha Validation failed: \(error.rawValue)", nil)
    }else {
      dispatchStepEventCompleted(stepName: "Captcha", result: [
        "response": result.captcha ?? ""
      ])
    }
  }
  
  func ocrData(addressFields: OcrAddress?) -> [String: String] {
    return [
      "city": addressFields?.city ?? "",
      "colony": addressFields?.colony ?? "",
      "postalCode": addressFields?.postalCode ?? "",
      "street": addressFields?.street ?? "",
      "state": addressFields?.state ?? "",
    ]
  }
  
  // TODO: ONBOARDING_SESSION_CREATED
  //MARK:- Generic event dispatching
  func dispatchStepEventCompleted(stepName: String, result: String) -> Void {
    print("Sttep event coompleted \(stepName) \(result)")
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_COMPLETED", body: [
      "module": stepName,
      "result": result
    ])
  }
  
  func dispatchStepEventError(stepName: String, result: String) -> Void {
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_ERROR", body: [
      "module": stepName,
      "status": result
    ])
  }
  
  func dispatchStepEventError(stepName: String, result: [String: Any]) -> Void {
    print("Step event coompleted with error \(stepName) \(result)")
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_ERROR", body: [
      "module": stepName,
      "result": result
    ])
  }
  
  func dispatchStepEventCompleted(stepName: String, result: [String: Any]) -> Void {
    print("Sttep event coompleted \(stepName) \(result)")
    EventEmitter.sharedInstance.dispatch(name: "ONBOARDING_STEP_COMPLETED", body: [
      "module": stepName,
      "result": result
    ])
  }
  
}
