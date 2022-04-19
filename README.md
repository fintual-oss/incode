# React Native Wrapper Usage guide

## React Native project setup

1) Setup the dev environment:
https://reactnative.dev/docs/environment-setup 

2) Run in the project root

```sh
npm install
```

If it fails due to 'bob' or 'bob builder', install the package:

```sh
npm install react-native-builder-bob 
```

## Native modules setup

1) Go to 'example' folder and run `npm install`

```sh
cd example
npm install
```

This will install needed react native dependencies for Android/iOS projects

## Additional setup for iOS

3. Cocoapods install:

```sh
cd ios && pod install
```

## Running on device

https://reactnative.dev/docs/running-on-device   

### Android

```sh
adb devices
```

You'll see your device id:
```
List of devices attached
YOUR_DEVICE_ID  device
```

Run this command:
```
adb -s YOUR_DEVICE_ID reverse tcp:8081 tcp:8081
```

Start Metro server

```
react-native start
```

Run the app:

```
react-native run-android
```

### iOS

Open .xcworkspace file in Xcode and run the the app from Xcode. Metro server will be run automatically.

Note: Clean install might throw some error before permission is given to access local Network in order to communicate with Metro server. After permission is allowed subsequent runs should work fine.


## Pushing changes

When commiting files, add `--no-verify` flag, ie. `git commit -m "My change" --no-verify`, to avoid lint checkers that looks for warnings which we have a decent amount atm.


