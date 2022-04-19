import { NativeModules, NativeEventEmitter } from 'react-native';

type ModuleEnabled = {
  /**
   * Explicitly enable or disable this module in the onboarding process.
   * Default: true
   */
  enabled?: boolean;
};

type Module =
  | 'Phone'
  | 'DocumentScan'
  | 'Geolocation'
  | 'Signature'
  | 'VideoSelfie'
  | 'IdScan'
  | 'Conference'
  | 'SelfieScan'
  | 'FaceMatch'
  | 'QrScan'
  | 'Captcha'
  | 'Approve'
  | 'UserScore'
  | 'GovernmentValidation'
  | 'UserConsent';

export type IncodeModuleConfig =
  | { module: 'Phone' }
  | {
      module: 'DocumentScan';
      /** defaults to `false` */
      showTutorial?: boolean;
      /** defaults to `true` */
      showDocumentProviderScreen?: boolean;
    }
  | { module: 'Geolocation' }
  | { module: 'Signature' }
  | {
      module: 'VideoSelfie';

      /** default: `true` */
      showTutorial?: boolean;
      /** default: 'selfieMatch' */
      selfieScanMode?: 'selfieMatch' | 'faceMatch';
      /** default: 'false' */
      selfieLivenessCheck?: boolean;
      /** default: 'true' */
      showIdScan?: boolean;
      /** default: 'true' */
      showDocumentScan?: boolean;
      /** default: 'true' */
      showVoiceConsent?: boolean;
      /** default: 3 */
      voiceConsentQuestionsCount?: number;
    }
  | {
      module: 'IdScan';
      /**
       * defaults to `true`
       */
      showTutorial?: boolean;
      /** optional. If omitted, the app will display a chooser to the user. */
      idType?: 'id' | 'passport';
    }
  | {
      module: 'Conference';
      /**
       * Defaults to `true`
       */
      disableMicOnStart?: boolean;
    }
  | {
      module: 'SelfieScan';
      /**
       * Defaults to `true`
       */
      showTutorial?: boolean;
    }
  | { module: 'FaceMatch' }
  | { module: 'QrScan' }
  | { module: 'Captcha' }
  | {
      module: 'Approve';
      /** default: `false` */
      forceApproval?: boolean;
    }
  | {
      module: 'UserScore';
      /** default: `accurate` */
      mode?: 'accurate' | 'fast';
    }
  | {
      module: 'GovernmentValidation';
    }
  | {
      module: 'UserConsent';
      title: string;
      content: string;
    };

export type OnboardingConfig = {
  config: (IncodeModuleConfig & ModuleEnabled)[];
};

type ResultErrorCodes = 'error' | 'userCancelled' | 'emulatorDetected';
type ResultCodes = 'success' | ResultErrorCodes;

type Address = {
  city: string;
  colony: string;
  postalCode: string;
  street: string;
  state: string;
};

type IdScanOcrData = any;

export type OnboardingResponse = {
  status: 'success' | 'userCancelled';
};

/**
 * After 3rd failure of taking a shot of the ID
 */
type IdValidationFailure =
  | 'errorClassification'
  | 'noFacesFound'
  /** not in android */
  | 'errorCropQuality'
  | 'errorGlare'
  | 'errorReadability'
  | 'errorSharpness'
  | 'errorTypeMismatch'
  /** android only */
  | 'userCancelled'
  | 'unknownError'
  | 'errorClassification'
  | 'shadow'
  | 'errorAddress'
  | 'errorPassportClassification'
  | 'errorReadability';

type IdValidationStatus = IdValidationFailure | 'ok';

interface PhoneStepCompleteEvent {
  module: 'Phone';
  result: { phone: string; resultCode: ResultCodes };
}

//TODO: not really
export interface DocumentScanCompleteEvent {
  module: 'DocumentScan';
  result: {
    status: 'error' | 'invalidSession' | 'success' | 'userCancelled';
    address: string;
    image: IncdImage;
    insuranceCard: {
      copayEr: string;
      copayOv: string;
      member: string;
      memberId: string;
      provider: string;
      rxBin: string;
      rxPcn: string;
    };
  };
}

export interface GeolocationCompleteEvent {
  module: 'Geolocation';
  result: Address;
}

export interface SignatureCompleteEvent {
  module: 'Signature';
  result: { status: 'success' };
}
export interface VideoSelfieCompleteEvent {
  module: 'VideoSelfie';
  result: {
    // @TODO: define this
  };
}

export interface QrScanCompleteEvent {
  module: 'QrScan';
  result: {
    idCic: string;
  };
}

export interface IdScanCompleteEvent {
  module: 'IdScan';
  result: {
    /**
     * Not present in case `front.status` or `back.status` is non-`ok`
     */
    data?: IdScanOcrData;
    extendedOcrData: string;
    images?: {
      front: IncdImage;
      back: IncdImage;
    };
    status: {
      front: IdValidationStatus;
      back: IdValidationStatus;
    };
  };
}

export interface ConferenceCompleteEvent {
  module: 'Conference';
  result: {
    status: 'success';
  };
}

export type IncdImage = {
  /** 64-bit encoded image data */
  pngBase64?: string;
};
export interface SelfieScanCompleteEvent {
  module: 'SelfieScan';
  result: {
    status: 'success';
    spoofAttempt: boolean;
    image: IncdImage;
  };
}
export interface FaceMatchCompleteEvent {
  module: 'FaceMatch';
  result: {
    status: 'matched' | 'mismatch';
  };
}

export interface UserScoreCompleteEvent {
  module: 'UserScore';
  result: UserScore;
}

export interface CaptchaCompleteEvent {
  module: 'UserScore';
  result: {
    status: 'success';
    response: string;
  };
}

export type ApprovalResult = {
  approved: 'approved' | 'failed';
  id: string;
  customerToken: string;
};

export type FaceLoginResult = { faceMatched: boolean; spoofAttempt: boolean };

export interface ApproveCompleteEvent {
  module: 'Approve';
  result: ApprovalResult;
}

export interface UserConsentCompleteEvent {
  module: 'UserConsent';
  result: { status: 'success' };
}

export type StepCompletedEvent =
  | PhoneStepCompleteEvent
  | DocumentScanCompleteEvent
  | GeolocationCompleteEvent
  | SignatureCompleteEvent
  | VideoSelfieCompleteEvent
  | QrScanCompleteEvent
  | IdScanCompleteEvent
  | ConferenceCompleteEvent
  | SelfieScanCompleteEvent
  | FaceMatchCompleteEvent
  | UserScoreCompleteEvent
  | CaptchaCompleteEvent
  | ApproveCompleteEvent
  | UserConsentCompleteEvent;

type PhoneStepErrorEvent = {
  module: 'Phone';
  status: 'userCancelled' | 'invalidSession' | 'error';
};
type QRScanErrorEvent = {
  module: 'QrScan';
  result: {
    status: 'userCancelled' | 'invalidSession' | 'error';
  };
};
type DocumentScanErrorEvent = {
  module: 'DocumentScan';
  status: 'permissionsDenied' | 'simulatorDetected' | 'unknown';
};
type GeolocationErrorEvent = {
  module: 'Geolocation';
  status: 'unknownError' | 'permissionsDenied' | 'noLocationExtracted';
};
type SignatureErrorEvent = {
  module: 'Signature';
  status: 'error' | 'invalidSession';
};

type IdScanErrorEvent = {
  module: 'IdScan';
  data?: IdScanOcrData;
  status: {
    front: IdValidationStatus;
    back: IdValidationStatus;
  };
};

type ConferenceErrorEvent = {
  module: 'Conference';
  result: {
    status: 'userCancelled' | 'invalidSession' | 'error';
  };
};

type SelfieScanErrorEvent = {
  module: 'SelfieScan';
  status: 'none' | 'permissionsDenied' | 'simulatorDetected' | 'spoofDetected';
};

type FaceMatchErrorEvent = {
  module: 'FaceMatch';
  status: 'simulatorDetected' | 'unknownError';
};

type UserScoreErrorEvent = {
  module: 'UserScore';
  status: 'warning' | 'unknown' | 'manual' | 'fail';
};

type CaptchaErrorEvent = {
  module: 'Captcha';
  status: ResultErrorCodes;
};

type VideoScanErrorEvent = { module: 'VideoSelfie' };

export type StepErrorEvent =
  | PhoneStepErrorEvent
  | QRScanErrorEvent
  | DocumentScanErrorEvent
  | GeolocationErrorEvent
  | SignatureErrorEvent
  | VideoScanErrorEvent
  | IdScanErrorEvent
  | ConferenceErrorEvent
  | SelfieScanErrorEvent
  | FaceMatchErrorEvent
  | CaptchaErrorEvent
  | UserScoreErrorEvent;

export type ConferenceStepUpdateEvent = { module: 'Conference'; result: {} };
export type StepUpdatedEvent = ConferenceStepUpdateEvent;

export type StepUpdateListener = {
  module: 'Conference';
  listener: (e: ConferenceStepUpdateEvent) => void;
};
export interface UserScore {
  overallScore: string;
  status: 'ok' | 'warn' | 'unknown' | 'fail' | 'manual';
  facialRecognitionScore: string;
  existingUser: boolean;
  idVerificationScore: string;
  livenessOverallScore: string;
}

export type StepCompleteListener =
  | { module: 'Phone'; listener: (e: PhoneStepCompleteEvent) => void }
  | { module: 'DocumentScan'; listener: (e: DocumentScanCompleteEvent) => void }
  | { module: 'Geolocation'; listener: (e: GeolocationCompleteEvent) => void }
  | { module: 'Signature'; listener: (e: SignatureCompleteEvent) => void }
  | { module: 'VideoSelfie'; listener: (e: VideoSelfieCompleteEvent) => void }
  | { module: 'IdScan'; listener: (e: IdScanCompleteEvent) => void }
  | { module: 'Conference'; listener: (e: ConferenceCompleteEvent) => void }
  | { module: 'SelfieScan'; listener: (e: SelfieScanCompleteEvent) => void }
  | { module: 'FaceMatch'; listener: (e: FaceMatchCompleteEvent) => void }
  | { module: 'QrScan'; listener: (e: QrScanCompleteEvent) => void }
  | { module: 'Approve'; listener: (e: ApproveCompleteEvent) => void }
  | { module: 'Captcha'; listener: (e: CaptchaCompleteEvent) => void }
  | { module: 'UserScore'; listener: (e: UserScoreCompleteEvent) => void }
  | { module: 'UserConsent'; listener: (e: UserConsentCompleteEvent) => void };

export type StepErrorListener =
  | { module: 'Phone'; listener: (e: PhoneStepErrorEvent) => void }
  | { module: 'DocumentScan'; listener: (e: DocumentScanErrorEvent) => void }
  | { module: 'Geolocation'; listener: (e: GeolocationErrorEvent) => void }
  | { module: 'Signature'; listener: (e: SignatureErrorEvent) => void }
  | { module: 'VideoSelfie'; listener: (e: VideoScanErrorEvent) => void }
  | { module: 'IdScan'; listener: (e: IdScanErrorEvent) => void }
  | { module: 'Conference'; listener: (e: ConferenceErrorEvent) => void }
  | { module: 'SelfieScan'; listener: (e: SelfieScanErrorEvent) => void }
  | { module: 'FaceMatch'; listener: (e: FaceMatchErrorEvent) => void }
  | { module: 'QrScan'; listener: (e: QRScanErrorEvent) => void }
  | { module: 'Captcha'; listener: (e: CaptchaErrorEvent) => void }
  | { module: 'UserScore'; listener: (e: UserScoreErrorEvent) => void };

import {
  modulesConfigDefaultConfigEnhancement,
  resultsConversions,
} from './utils';

type IncodeSdkType = {
  initialize({
    testMode,
    apiConfig,
    disableHookCheck,
    sdkMode,
  }: {
    testMode?: boolean;
    apiConfig: {
      key: string;
      url: string;
      conferenceUrl?: string;
      regionCode?: 'ALL' | 'MX';
    };
    disableHookCheck?: boolean;
    sdkMode?: 'standard' | 'captureOnly';
  }): Promise<void>;
  startOnboarding({
    config,
    interviewId,
    configurationId,
  }: {
    config: (IncodeModuleConfig & ModuleEnabled)[];
    interviewId?: string;
    configurationId?: string;
  }): Promise<OnboardingResponse>;
  /**
   *
   * @param stepAndListener
   * @returns the unregister callback
   */
  onStepCompleted: (e: StepCompleteListener) => StepEventUnsubscriber;
  onStepUpdated: (
    listener: (e: StepUpdatedEvent) => void
  ) => StepEventUnsubscriber;
  onStepError: (e: StepErrorListener) => StepEventUnsubscriber;
  onSessionCreated: (
    listener: (e: { interviewId: Number }) => void
  ) => StepEventUnsubscriber;
  approve: (args: { forceApproval: boolean }) => Promise<ApprovalResult>;
  startFaceLogin: (args: {
    showTutorials: boolean;
    customerToken: string;
    customerUUID: string;
  }) => Promise<FaceLoginResult>;
  getUserScore: (config?: { mode?: 'accurate' | 'fast' }) => Promise<UserScore>;
  createOnboardingSession(config: {
    verifers?: Module[];
  }): Promise<{ token: string; interviewId: string; regionId: string }>;
  startOnboardingSection(config: OnboardingConfig): Promise<OnboardingResponse>;
  finishOnboardingFlow(): Promise<any>;
};

type StepEventUnsubscriber = () => void;

const IncodeSdk = (() => {
  const stepCompleteListeners: {
    [module: string]: any[];
  } = {};

  const stepUpdateListeners: {
    [module: string]: any[];
  } = {};

  const errorListeners: {
    [module: string]: any[];
  } = {};

  const addListener = (
    toListeners: { [module: string]: any[] },
    stepAndListener:
      | StepCompleteListener
      | StepErrorListener
      | StepUpdateListener
  ) => {
    const { module, listener } = stepAndListener;
    if (toListeners[module] === undefined) {
      toListeners[module] = [];
    }

    toListeners[module] = [...toListeners[module], listener];
    return () => {
      const ofModule = toListeners[module];
      const idx = ofModule.indexOf(listener);
      if (idx === -1) {
        return;
      }
      //removes the listeners
      toListeners[module] = [
        ...ofModule.slice(0, idx),
        ...ofModule.slice(idx + 1),
      ];
    };
  };

  const emitter = new NativeEventEmitter(NativeModules.IncodeSdk);

  // complete listener
  emitter.addListener('ONBOARDING_STEP_COMPLETED', (e: StepCompletedEvent) => {
    const ofModule = stepCompleteListeners[e.module];

    const forListener = resultsConversions(e);

    if (ofModule) {
      ofModule.forEach((listener: (_: StepCompletedEvent) => void) =>
        listener(forListener)
      );
    }
  });

  // error listener
  emitter.addListener('ONBOARDING_STEP_ERROR', (e: StepErrorEvent) => {
    const ofModule = errorListeners[e.module];
    if (ofModule) {
      ofModule.forEach((listener: (_: StepErrorEvent) => void) => listener(e));
    }
  });

  // error listener
  emitter.addListener('ONBOARDING_STEP_UPDATE', (e: StepUpdatedEvent) => {
    const ofModule = stepUpdateListeners[e.module];
    if (ofModule) {
      ofModule.forEach((listener: (_: StepUpdatedEvent) => void) =>
        listener(e)
      );
    }
  });

  const fixModuleConfigDefaultValues = (
    config: OnboardingConfig
  ): OnboardingConfig => ({
    ...config,
    config: config.config.map((moduleConfig) => {
      const enhanced = {
        ...moduleConfig,
        enabled:
          moduleConfig.enabled === undefined ? true : moduleConfig.enabled,
      };

      return modulesConfigDefaultConfigEnhancement(enhanced);
    }),
  });

  return {
    ...NativeModules.IncodeSdk,
    initialize: (config: { testMode: boolean }) => {
      return NativeModules.IncodeSdk.initialize(
        config.testMode === undefined ? { testMode: false } : config
      );
    },

    onSessionCreated: (listener: () => void): StepEventUnsubscriber => {
      const subscription = emitter.addListener(
        'ONBOARDING_SESSION_CREATED',
        listener
      );
      return subscription.remove;
    },

    onStepCompleted: (
      stepAndListener: StepCompleteListener
    ): StepEventUnsubscriber => {
      return addListener(stepCompleteListeners, stepAndListener);
    },

    onStepError: (
      stepAndListener: StepErrorListener
    ): StepEventUnsubscriber => {
      return addListener(errorListeners, stepAndListener);
    },

    onStepUpdated: (
      stepAndListener: StepUpdateListener
    ): StepEventUnsubscriber => {
      return addListener(stepUpdateListeners, stepAndListener);
    },

    startOnboarding: (
      config: OnboardingConfig
    ): Promise<OnboardingResponse> => {
      return NativeModules.IncodeSdk.startOnboarding(
        fixModuleConfigDefaultValues(config)
      );
    },
    startOnboardingSection: (
      config: OnboardingConfig
    ): Promise<OnboardingResponse> => {
      return NativeModules.IncodeSdk.startOnboardingSection(
        fixModuleConfigDefaultValues(config)
      );
    },
    startFaceLogin: (config: {
      showTutorials: boolean;
      customerToken: string;
      customerUUID: string;
    }): Promise<FaceLoginResult> => {
      return NativeModules.IncodeSdk.startFaceLogin(config);
    },
    getUserScore: (
      config: { mode?: 'accurate' | 'fast' } = { mode: 'accurate' }
    ) => {
      return NativeModules.IncodeSdk.getUserScore(config);
    },
  };
})();

export default IncodeSdk as IncodeSdkType;
