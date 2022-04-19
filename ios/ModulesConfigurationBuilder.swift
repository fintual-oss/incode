//
//  ModulesConfigurationBuilder.swift
//  react-native-incode-sdk
//
//  Created by Nenad Vitorovic on 2/17/21.
//

import Foundation
import IncdOnboarding

func aFlowConfig(forModules modules: NSArray, withFlowTag flowTag: String = "easyFlowTag", regionCode: String) -> IncdOnboardingConfiguration {
  
  func toFloat(it: Double?) -> Float? {
    if let dat = it {
      return Float(dat)
    }
    return nil;
  }
  
  func isModuleEnabled (_ module: NSDictionary) -> Bool {
    return module.value(forKey: "enabled") as! Bool
  }
  
  func shouldShowTutorial (_ module: NSDictionary) -> Bool {
    if let show = module.value(forKey: "showTutorial") as? Bool {
      return show
    }
    return true
  }
  
  func extractABoolean(_ moduleConfig: NSDictionary, forKey key: String) -> Bool? {
    return moduleConfig.value(forKey: key) as? Bool
  }
  
  let sessionConfig = IncdOnboardingConfiguration(regionCode: regionCode, queue: IncdOnboardingManager.shared.queue, waitForTutorials: true, configurationName: flowTag)
  
  modules.forEach({ (key: Any) in
    let moduleConfig = key as! NSDictionary
    let module = moduleConfig.value(forKey: "module") as! String
    
    let moduleEnabled = isModuleEnabled(moduleConfig)
    
    if(!moduleEnabled){
      return
    }
    let showTutorials = shouldShowTutorial(moduleConfig)
    
    switch module{
    case "Phone":
      sessionConfig.addPhone()
    case "IdScan":
      if let idType = moduleConfig.value(forKey: "idType") as? String {
        sessionConfig.addIdScan(showTutorials: showTutorials, idType: idType == "passport" ? .passport : .id)
      } else {
        sessionConfig.addIdScan(showTutorials: showTutorials)
      }
    case "DocumentScan":
      sessionConfig.addDocumentScan(showTutorials: showTutorials, showDocumentProviderOptions: moduleConfig.value(forKey: "showDocumentProviderScreen") as? Bool, documentType: .document)
    case "Geolocation":
      sessionConfig.addGeolocation()
    case "SelfieScan":
      sessionConfig.addSelfieScan(showTutorials: showTutorials, lensesCheck: nil, brightnessThreshold: nil)
    case "FaceMatch":
      sessionConfig.addFaceMatch()
    case "Signature":
      sessionConfig.addSignature()
    case "VideoSelfie":
      let videoSelfieConfiguration = VideoSelfieConfiguration()
      videoSelfieConfiguration.tutorials(enabled: showTutorials)
      
      var selfieMode: VideoSelfieConfiguration.SelfieMode? = nil
      if let selfieScanMode = moduleConfig.value(forKey: "selfieScanMode") as? String {
        if selfieScanMode == "faceMatch" {
          selfieMode = .faceMatch
        } else if selfieScanMode == "selfieMatch" {
          selfieMode = .selfieMatch
        }
      }

      let livenessCheck = moduleConfig.value(forKey: "selfieLivenessCheck") as? Bool
      
      if let livenessCheck = livenessCheck,
         let selfieMode = selfieMode {
      videoSelfieConfiguration.selfieScan(performLivenessCheck: livenessCheck, mode: selfieMode)
      } else if let livenessCheck = livenessCheck {
        videoSelfieConfiguration.selfieScan(performLivenessCheck: livenessCheck)
      } else if let selfieMode = selfieMode {
        videoSelfieConfiguration.selfieScan(performLivenessCheck: false, mode: selfieMode)
      }
      
      if let showIdScan = moduleConfig.value(forKey: "showIdScan") as? Bool {
        videoSelfieConfiguration.idScan(enabled: showIdScan)
      }
      if let poa = moduleConfig.value(forKey: "showDocumentScan") as? Bool {
        videoSelfieConfiguration.documentScan(enabled: poa)
      }
      
      if let showVoiceConsent = moduleConfig.value(forKey: "showVoiceConsent") as? Bool {
        if let questionsCount = moduleConfig.value(forKey: "voiceConsentQuestionsCount") as? Int {
          videoSelfieConfiguration.voiceConsent(enabled: showVoiceConsent, questionsCount: questionsCount)
        } else {
          videoSelfieConfiguration.voiceConsent(enabled: showVoiceConsent);
        }
      }
      sessionConfig.addVideoSelfie(videoSelfieConfiguration: videoSelfieConfiguration)
    case "Conference":
      sessionConfig.addVideoConference(disableMicOnCallStarted: extractABoolean(moduleConfig, forKey: "disableMicOnCallStarted"))
    case "Approve":
      sessionConfig.addApproval(forceApproval: extractABoolean(moduleConfig, forKey: "forceApproval"))
    case "UserScore":
      let modeName = moduleConfig.value(forKey: "mode") as? String
      let mode = (modeName == "fast") ? UserScoreFetchMode.fast : UserScoreFetchMode.accurate
      sessionConfig.addUserScore(userScoreFetchMode: mode)
      break;
    case "QrScan":
      sessionConfig.addQRScan(showTutorials: extractABoolean(moduleConfig, forKey: "showTutorials"))
    case "Captcha":
      sessionConfig.addCaptcha();
    case "GovernmentValidation":
      sessionConfig.addGovernmentValidation();
    case "UserConsent":
      if let title = moduleConfig.value(forKey: "title") as? String,
         let content = moduleConfig.value(forKey: "content") as? String {
        sessionConfig.addUserConsent(title: title, content: content)
      } else {
        print("User Consent title and content missing")
      }
    default:
      print("Unknown config option \(module)")
    }
  })
  return sessionConfig
}
