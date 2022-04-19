export const ifUndefined = (field, defaultValue) => field === undefined ? defaultValue : field;
const moduleDefaultsFixers = {
  Approve: config => {
    if (config.module === 'Approve' && config.forceApproval === undefined) {
      return { ...config,
        forceApproval: false
      };
    }

    return config;
  },
  UserScore: config => {
    if (config.module === 'UserScore' && config.mode === undefined) {
      return { ...config,
        mode: 'accurate'
      };
    }

    return config;
  },
  SelfieScan: config => {
    if (config.module === 'SelfieScan') {
      var ret = config;

      if (config.showTutorial === undefined) {
        ret = { ...ret,
          showTutorial: true
        };
      }

      return ret;
    }

    return config;
  },
  DocumentScan: config => {
    if (config.module === 'DocumentScan' && config.showTutorial === undefined) {
      return { ...config,
        showTutorial: true
      };
    }

    return config;
  },
  Conference: config => {
    if (config.module === 'Conference' && config.disableMicOnStart === undefined) {
      return { ...config,
        disableMicOnStart: true
      };
    }

    return config;
  },
  VideoSelfie: config => {
    if (config.module === 'VideoSelfie') {
      return { ...config,
        showTutorial: ifUndefined(config.showTutorial, true),
        selfieLivenessCheck: ifUndefined(config.selfieLivenessCheck, false),
        selfieScanMode: ifUndefined(config.selfieScanMode, 'selfieMatch'),
        showIdScan: ifUndefined(config.showIdScan, true),
        showDocumentScan: ifUndefined(config.showDocumentScan, true),
        showVoiceConsent: ifUndefined(config.showVoiceConsent, true),
        voiceConsentQuestionsCount: ifUndefined(config.voiceConsentQuestionsCount, 3)
      };
    }

    return config;
  }
};
export const modulesConfigDefaultConfigEnhancement = config => {
  switch (config.module) {
    case 'VideoSelfie':
      return moduleDefaultsFixers.VideoSelfie(config);

    case 'Conference':
      return moduleDefaultsFixers.Conference(config);

    case 'SelfieScan':
      return moduleDefaultsFixers.SelfieScan(config);

    case 'Approve':
      return moduleDefaultsFixers.Approve(config);

    case 'UserScore':
      return moduleDefaultsFixers.UserScore(config);

    case 'DocumentScan':
      return moduleDefaultsFixers.DocumentScan(config);
  }

  return config;
};

const numToDate = d => {
  if (d === undefined) {
    return undefined;
  }

  const num = typeof d === 'string' ? Number.parseInt(d, 10) : d;
  return new Date(num);
};

const toIntMaybe = d => {
  if (typeof d === 'number') {
    return d;
  }

  if (typeof d === 'string') {
    return Number.parseInt(d, 10);
  }

  return -1;
};

const idScanConversion = e => {
  // @ts-ignore
  const rawOcr = e.result.ocrData; // Android SDK not supporting it yet - skip all of it

  if (!rawOcr) {
    return e;
  }

  const transformedEvent = { ...e,
    result: { ...e.result,
      data: JSON.parse(rawOcr).ocrData
    }
  }; // @ts-ignore

  delete transformedEvent.result['ocrData']; // means an error in transforming it

  if (!transformedEvent.result.data) {
    return transformedEvent;
  }

  const ret = { ...transformedEvent,
    result: { ...transformedEvent.result,
      data: { ...transformedEvent.result.data,
        birthDate: numToDate(transformedEvent.result.data.birthDate),
        addressStatementEmissionDate: numToDate(transformedEvent.result.data.addressStatementEmissionDate),
        issueDate: toIntMaybe(transformedEvent.result.data.issueDate),
        expirationDate: toIntMaybe(transformedEvent.result.data.expirationDate),
        expireAt: numToDate(transformedEvent.result.data.expireAt),
        images: e.result.data.images
      }
    }
  };
  return ret;
};

export const resultsConversions = e => {
  if (e.module === 'IdScan') {
    return idScanConversion(e);
  }

  return e;
};
//# sourceMappingURL=utils.js.map