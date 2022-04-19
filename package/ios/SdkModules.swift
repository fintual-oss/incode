//
//  SdkModules.swift
//  react-native-incode-sdk
//
//  Created by Nenad Vitorovic on 2/17/21.
//

import Foundation
import IncdOnboarding

enum SdkSteps: String {
    case Phone = "Phone"
    case IdScan = "IdScan"
    case DocumentScan = "DocumentScan"
    case Geolocation = "Geolocation"
    case SelfieScan = "SelfieScan"
    case FaceMath = "FaceMatch"
    case Signature = "Signature"
    case VideoSelfie = "VideoSelfie"
}

func addToFlow(step: SdkSteps, config: IncdOnboardingFlowConfiguration) -> IncdOnboardingFlowConfiguration {
    switch step {
        case .Phone: config.addPhone()
        case .IdScan: config.addIdScan()
        case .DocumentScan: config.addDocumentScan(documentType: .document)
        case .SelfieScan: config.addSelfieScan()
        case .FaceMath: config.addFaceMatch()
        case .Signature: config.addSignature()
        case .VideoSelfie: config.addVideoSelfie(videoSelfieConfiguration: VideoSelfieConfiguration())
        case .Geolocation: config.addGeolocation()
    }
    return config
}

func userScoreAsDict(_ userScore: UserScore) -> [String: Any]{
    func maybeToString(_ maybe: Any?) -> String {
        if let it = maybe {
            return "\(it)"
        }
        return ""
    }
    return [
        "overallScore": userScore.overall?.value ?? "",
        "status": maybeToString(userScore.overall?.status),
        "facialRecognitionScore": maybeToString(userScore.faceRecognition?.overall?.value),
        "existingUser": maybeToString(userScore.faceRecognition?.existingUser ?? nil),
        "idVerificationScore": maybeToString(userScore.idValidation?.overall?.value),
        "livenessOverallScore": maybeToString(userScore.liveness?.overall?.value)
    ]
}
