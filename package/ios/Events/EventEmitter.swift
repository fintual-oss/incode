//
//  EventEmitter.swift
//  CocoaAsyncSocket
//
//  Created by Nenad Vitorovic on 2/22/21.
//

import Foundation
import React

class EventEmitter{
    /// Shared Instance.
    public static var sharedInstance = EventEmitter()

    // ReactNativeEventEmitter is instantiated by React Native with the bridge.
    private static var eventEmitter: RCTEventEmitter!

    private init() {}

    // When React Native instantiates the emitter it is registered here.
    func registerEventEmitter(eventEmitter: RCTEventEmitter) {
        EventEmitter.eventEmitter = eventEmitter
    }

    func dispatch(name: String, body: Any?) {
        EventEmitter.eventEmitter.sendEvent(withName: name, body: body)
    }

    /// All Events which must be support by React Native.
    lazy var allEvents: [String] = {
        return ["ONBOARDING_STEP_COMPLETED", "ONBOARDING_STEP_UPDATE", "ONBOARDING_CANCELLED", "ONBOARDING_COMPLETE", "ONBOARDING_STEP_ERROR", "ONBOARDING_SESSION_CREATED"]
    }()

}
