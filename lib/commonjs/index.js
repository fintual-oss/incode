"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _reactNative = require("react-native");

var _utils = require("./utils");

const IncodeSdk = (() => {
  const stepCompleteListeners = {};
  const stepUpdateListeners = {};
  const errorListeners = {};

  const addListener = (toListeners, stepAndListener) => {
    const {
      module,
      listener
    } = stepAndListener;

    if (toListeners[module] === undefined) {
      toListeners[module] = [];
    }

    toListeners[module] = [...toListeners[module], listener];
    return () => {
      const ofModule = toListeners[module];
      const idx = ofModule.indexOf(listener);

      if (idx === -1) {
        return;
      } //removes the listeners


      toListeners[module] = [...ofModule.slice(0, idx), ...ofModule.slice(idx + 1)];
    };
  };

  const emitter = new _reactNative.NativeEventEmitter(_reactNative.NativeModules.IncodeSdk); // complete listener

  emitter.addListener('ONBOARDING_STEP_COMPLETED', e => {
    const ofModule = stepCompleteListeners[e.module];
    const forListener = (0, _utils.resultsConversions)(e);

    if (ofModule) {
      ofModule.forEach(listener => listener(forListener));
    }
  }); // error listener

  emitter.addListener('ONBOARDING_STEP_ERROR', e => {
    const ofModule = errorListeners[e.module];

    if (ofModule) {
      ofModule.forEach(listener => listener(e));
    }
  }); // error listener

  emitter.addListener('ONBOARDING_STEP_UPDATE', e => {
    const ofModule = stepUpdateListeners[e.module];

    if (ofModule) {
      ofModule.forEach(listener => listener(e));
    }
  });

  const fixModuleConfigDefaultValues = config => ({ ...config,
    config: config.config.map(moduleConfig => {
      const enhanced = { ...moduleConfig,
        enabled: moduleConfig.enabled === undefined ? true : moduleConfig.enabled
      };
      return (0, _utils.modulesConfigDefaultConfigEnhancement)(enhanced);
    })
  });

  return { ..._reactNative.NativeModules.IncodeSdk,
    initialize: config => {
      return _reactNative.NativeModules.IncodeSdk.initialize(config.testMode === undefined ? {
        testMode: false
      } : config);
    },
    onSessionCreated: listener => {
      const subscription = emitter.addListener('ONBOARDING_SESSION_CREATED', listener);
      return subscription.remove;
    },
    onStepCompleted: stepAndListener => {
      return addListener(stepCompleteListeners, stepAndListener);
    },
    onStepError: stepAndListener => {
      return addListener(errorListeners, stepAndListener);
    },
    onStepUpdated: stepAndListener => {
      return addListener(stepUpdateListeners, stepAndListener);
    },
    startOnboarding: config => {
      return _reactNative.NativeModules.IncodeSdk.startOnboarding(fixModuleConfigDefaultValues(config));
    },
    startOnboardingSection: config => {
      return _reactNative.NativeModules.IncodeSdk.startOnboardingSection(fixModuleConfigDefaultValues(config));
    },
    startFaceLogin: config => {
      return _reactNative.NativeModules.IncodeSdk.startFaceLogin(config);
    },
    getUserScore: function () {
      let config = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {
        mode: 'accurate'
      };
      return _reactNative.NativeModules.IncodeSdk.getUserScore(config);
    }
  };
})();

var _default = IncodeSdk;
exports.default = _default;
//# sourceMappingURL=index.js.map