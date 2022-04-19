import React
import IncdOnboarding

/**
 Test cases:
 1. Missing Incode-Info.plst
 2. Corrrupt Incode-Info.plst
 */

@objc(IncodeSdk)
class IncodeSdk: RCTEventEmitter {
    var regionCode: String?
    
    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
    }
    
    // MARK: -EVENT EMMITTER CONFIG
    /// - Returns: all supported events
    @objc open override func supportedEvents() -> [String] {
        return EventEmitter.sharedInstance.allEvents
    }
    
    
    // MARK: - Init SDK
    @objc(initialize:withResolver:withRejecter:)
    func initialize(config: NSDictionary, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        let testMode = config.value(forKey: "testMode") as? Bool
        let sdkMode = config.value(forKey: "sdkMode") as? String
        let apiConfg = config.value(forKey: "apiConfig") as? NSDictionary
        
        guard apiConfg != nil else {
            reject("INCD:CNF", "Missing or incomplete apiConfig", nil)
            return
        }
        
        guard apiConfg?.value(forKey: "url") != nil &&  apiConfg?.value(forKey: "key") != nil else {
            reject("INCD:CNF", "Missing or incomplete apiConfig", nil)
            return
        }
        
        let apiUrl = apiConfg?.value(forKey: "url") as! String
        let apiKey = apiConfg?.value(forKey: "key") as! String
        let _ = apiConfg?.value(forKey: "conferenceUrl") as? String
        self.regionCode = apiConfg?.value(forKey: "defaultRegionCode") as? String ?? "ALL"
      
      
        
      DispatchQueue.main.async {
        if let sdkMode = sdkMode {
          if sdkMode == "standard" {
            IncdOnboardingManager.shared.sdkMode = .standard
          } else if sdkMode == "captureOnly" {
            IncdOnboardingManager.shared.sdkMode = .captureOnly
          }
        }
        IncdOnboardingManager.shared.initIncdOnboarding(url: apiUrl, apiKey: apiKey, loggingEnabled: true, testMode: testMode ?? false) {(success, error) in
          if success == true {
            resolve("Incdode SDK init complete")
          }else {
            switch error {
            case .simulatorDetected:
              print("Run on device")
              reject("Incd:BadDevice", "Incode SDK has to be ran on a device. If you would like to run on a simulator, please provide `testMode` parameter to true", nil)
            case .invalidInitParams:
              reject("Incd:BadConfig", "The Incode IOS project is not properly configured. Check content of the configuration file Incode-Info.plst \(error)", nil)
            case .testModeEnabled:
              reject("INCD::BadInit", "running on a device with testEnabled=true is not possible", nil)
            default:
              reject("Incd:BadInit", "\(error)", nil) // @TODO : uncrecognized errors
            }
          }
        }
      }
    }
    
    func aLoadingNavVc(inVc vc: UIViewController, noLoader: Bool = false) -> (UIViewController, (()-> Void)) {
//        let navVc = UINavigationController()
//        navVc.view.backgroundColor = .white
        var act: UIActivityIndicatorView?
        if !noLoader {
            act = UIActivityIndicatorView()
            act!.startAnimating()
//            navVc.view.addSubview(act!)
            vc.view.addSubview(act!)
            act!.pinEdgesToSuperview()
        }
//        navVc.modalPresentationStyle = .fullScreen
//        navVc.isNavigationBarHidden = true
//        navVc.setNavigationBarHidden(true, animated: true)
//        vc.present(navVc, animated: true)
        
//        return (navVc, {
          return (vc, {
            if(noLoader){ return }
            DispatchQueue.main.async {
                act!.removeFromSuperview()
            }
        })
    }
    
    // MARK: - Start onboarding
  @objc(startOnboarding:withResolver:withRejecter:)
  func startOnboarding(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
    print("Configuring onboarding with: \(config as Any)");
    
    DispatchQueue.main.async {
      guard let vc: UIViewController = RCTPresentedViewController()
      else {
        reject("IncdSDK:startOnboarding", "SDK onboardng started outside of a ViewController. This may indicate an issue with Incode's React Native SDK. Please contact Incode.", NSError())
        return
      }
      
      //            let stage = self.aLoadingNavVc(inVc: vc)
      let modulesConfig = config?.value(forKey: "config") as! NSArray
      let interviewId = config?.value(forKey: "interviewId") as? String
      let configurationId = config?.value(forKey: "configurationId") as? String
      print("configurationId: \(configurationId)")
      self.newFlowSession(presentedIn: vc, sessionConfig: aFlowConfig(forModules: modulesConfig, regionCode: self.regionCode ?? "ALL"), interviewId: interviewId, configurationId: configurationId, resolve: resolve, reject: reject)
    }
  }
    
    @objc(createOnboardingSession:withResolver:withRejecter:)
    func createOnboardingSession(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            // TODO: add modules
            IncdOnboardingManager.shared.createNewOnboardingSession(config: aFlowConfig(forModules: NSArray(), regionCode: "ALL"), onboardingValidationModules: nil) { (result) in
                if result.error != nil {
                    reject("IncdSDK:createFlow", "Incode flow creation failed", NSError())
                }else {
                    resolve([
                        "interviewId": result.interviewId!,
                        "token": result.token!,
                        "regionCode": result.regionCode ?? ""
                    ])
                }
            }
        }
    }
    

    @objc(startOnboardingSection:withResolver:withRejecter:)
    func startOnboardingSection(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        
        DispatchQueue.main.async {
            guard let vc: UIViewController = RCTPresentedViewController()
            else {
                reject("IncdSDK:startOnboardingSection", "SDK onboardng sections started outside of a ViewController. This may indicate an issue with Incode's React Native SDK. Please contact Incode.", NSError())
                return
            }
            
//            let stage = self.aLoadingNavVc(inVc: vc, noLoader: true)
            
            let modulesConfig = config!.value(forKey: "config") as! NSArray
            let config = aFlowConfig(forModules: modulesConfig, regionCode: "ALL")
            IncdOnboardingManager.shared.presentingViewController = vc
            IncdOnboardingManager.shared.startOnboardingSection(interviewId: nil, flowConfig: config, delegate: RCTINCOnboardingDelegate(resolve: resolve, reject: reject, incdSdk: vc))
        }
    }
    @objc(finishOnboardingFlow:withRejecter:)
    func finishOnboardingFlow(resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        IncdOnboardingManager.shared.finishFlow { (success, error)  in
            if(success){
                resolve([]);
            }else{
                reject("IncdSDK:finishFlow", "Error", NSError())
            }
        }
    }
    
    @objc(getUserScore:withResolver:withRejecter:)
    func getUserScore(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        let mode = (config?.value(forKey: "mode") as? String == "fast") ? UserScoreFetchMode.fast : UserScoreFetchMode.accurate
        print("Get User Score \(mode)")
        DispatchQueue.main.async {
            IncdOnboardingManager.shared.getUserScore(userScoreFetchMode: mode) { (userScore) in
                resolve(userScoreAsDict(userScore))
            }
        }
    }
    
    @objc(approve:withResolver:withRejecter:)
    func approve(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {

            IncdOnboardingManager.shared.startApprove(forceApproval: config?.value(forKey: "forceApproval") as? Bool ?? false, interviewId: nil) { (result) in
                resolve([
                    "status": result.success ? "approved" : "failed",
                    "customerToken": result.customerToken ?? "",
                    "id": result.uuid ?? ""
                ])
            }
        }
    }
  
  @objc(startFaceLogin:withResolver:withRejecter:)
  func startFaceLogin(config: NSDictionary?, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
    
    let showTutorials = config?.value(forKey: "showTutorials") as? Bool ?? true
    let customerUUID = config?.value(forKey: "customerUUID") as? String
    
    DispatchQueue.main.async {
      guard let vc: UIViewController = RCTPresentedViewController()
      else {
        reject("IncdSDK:startFaceLogin", "Face login started outside of a ViewController. This may indicate an issue with Incode's React Native SDK. Please contact Incode.", NSError())
        return
      }

      IncdOnboardingManager.shared.presentingViewController = vc
      IncdOnboardingManager.shared.startFaceLogin(showTutorials: showTutorials, customerUUID: customerUUID, lensesCheck: nil, brightnessThreshold: nil) { result in
          resolve([
            "faceMatched": result.faceLoginResult?.success == true ? true : false,
            "spoofAttempt": result.spoofAttempt == true ? true : false
          ])
      }
    }
    
  }
  
  func newFlowSession(presentedIn vc: UIViewController, sessionConfig: IncdOnboardingConfiguration, interviewId: String? = nil, configurationId: String? = nil, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
    IncdOnboardingManager.shared.presentingViewController = vc
    IncdOnboardingManager.shared.startOnboarding(config: sessionConfig, interviewId: interviewId, configurationId: configurationId, delegate: RCTINCOnboardingDelegate(resolve: resolve, reject: reject, incdSdk: vc))
    }
}
