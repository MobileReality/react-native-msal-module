# react-native-msal-module


# MSAL authorization library for React Native


> Looking for the MSAL plugin for React Native, here you are

Based on great plugin but deprecated [react-native-msal-plugin](https://github.com/rmcfarlane82/react-native-msal-plugin)

## **Important!**

### Package has been tested only on React Native 0.59.9 and 0.60+.
<a href="https://www.npmjs.com/package/react-native-msal-module"><img src="https://img.shields.io/badge/version-0.1.3-brightgreen" alt="NPM version"></a>
 <a href="/LICENSE"><img src="https://img.shields.io/badge/License-MIT-orange" alt="License"></a>


Support

- React Native 0.59.9 and 0.60+ ✅
- iOS 13 (tested) ✅
- AndroidX (tested) ✅
- Typescript ✅

## Instalation

`$ npm install react-native-msal-module --save`

> Automatic linking is not tested. We recommend manual linking

#### iOS

Setting the plugin requires adding proper properties in Info.plist and AppDelegate.m . Here is
 the [documentation](https://github.com/AzureAD/microsoft-authentication-library-for-android) of MSAL for iOS client. More in second point below.

- We provide only Cocoapods linking. In your pod file add:
```
  ...
  # Lines below
  pod 'MSAL', '~> 1.0.3' # <~ This line 
  pod 'react-native-msal-module', :path => '../node_modules/react-native-msal-module' # <~  this line
  
  ...

  end
```
- After that `pod install`

- You need to implement **Configuring MSAL** [step](https://github.com/AzureAD/microsoft-authentication-library-for-objc#configuring-msal)
    1. Make sure you have done [Adding MSAL to your project](https://github.com/AzureAD/microsoft-authentication-library-for-objc#adding-msal-to-your-project) (3 points)
    2. Make suer you have done [iOS only steps](https://github.com/AzureAD/microsoft-authentication-library-for-objc#ios-only-steps) (**2 points**)
    3. Add code below to your `AppDelegate.m` (3rd point of [iOS only steps](https://github.com/AzureAD/microsoft-authentication-library-for-objc#ios-only-steps))
    ```
    #import <React/RCTBridge.h>
    #import <React/RCTBundleURLProvider.h>
    #import <React/RCTRootView.h>
  
    #import <MSAL/MSAL.h> // <~ add this line(header file from MSAL)
  
  ...
  
  // this section, starting from here
   - (BOOL)application:(UIApplication *)application
               openURL:(NSURL *)url
               options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
   {
       return [MSALPublicClientApplication handleMSALResponse:url
                                            sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]];
   }
  // ending here
   ```

That's all!

#### Android

First of all, according to the Android [documentation](https://github.com/AzureAD/microsoft-authentication-library-for-android) and this plugin you should implement steps below:

- Add file `msal_config.json` ([draft](https://github.com/AzureAD/microsoft-authentication-library-for-android#step-2-create-your-msal-configuration-file)) in `assets` directory in `[your-project]/android/app/src/main`

    -  **remember** that `redirect_uri` includes `YOUR_BASE64_URL_ENCODED_PACKAGE_SIGNATURE` so you need BASE64 format of SIGNATURE parsed into URL. The same thing should be added in your Azure panel under redirect uris section

    - Also remember that your release signature differ with debug signature
- Implement this [step](https://github.com/AzureAD/microsoft-authentication-library-for-android#step-3-configure-the-androidmanifestxml) in your `AndroidManifest.xml`

- Linking:

    - in `[your-project]/android/app/build.gradle` add:
    ```
  dependencies {
  ...
  implementation project(':react-native-msal-module') // <~ add this line
  ...
  }
  ```
  - in `[your-project]/android/settings.gradle` add:
  ```
  rootProject.name = '<NAME_OF_YOUR_PROJECT>'
  ...
  include ':react-native-msal-module' // <~ add this line
  project(':react-native-msal-module').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-msal-module/android') // <~ add this line
  ...
  
  include ':app'
  ```
  **If your project is based on RN v0.60+ you can omit step below as you will overwrite already automatically linked package**  
  - in `[your-project]/android/app/src/main/java/com/[your-project]/MainApplication.java` add:
  ```
  package <YOUR_APP_PACKAGE>
  ...
  import com.reactlibrary.RNMsalModulePackage; // <~ add this line
  ...
  
  public class MainApplication extends Application implements ReactApplication {

  ...
   @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            ...
            new RNMsalModulePackage(), // <~ add this line
            ...
        );
      }
  ...
  ```
 That's all, you did it!
  
## Usage


### Initialization
```javascript
import React from 'react';
import { View, Text } from 'react-native';
import MSAL from 'react-native-msal-module';

export class App extends React.Component {
    componentDidMount() {
        MSAL.init(<YOUR_CLIENT_ID>);
    }
    
    render() {
       <View>
            <Text>App</Text> 
       </View>
    }

};
```

### Acquire token
```
    acquireTokenAsync: (scopes: string[]) => Promise<MSALResponse>;
```
```javascript
import React from 'react';
import { View, Text } from 'react-native';
import MSAL from 'react-native-msal-module';

export class App extends React.Component {
    async componentDidMount() {
        // Acquire token can be executed only after successful initalization of MSAL
        // Pass needed scopes
        const msalResponse = await MSAL.acquireTokenAsync(scopes);
    }
    render() {
       <View>
            <Text>App</Text> 
       </View>
    }
}
```

#### MSAL reponse
| Parameter     | type | 
| ---------------------- | :---: |
| **accessToken**                  | String | 
| **idToken**              | String |
| **userId**              | String |
| **expiresOn**              | String |
| **userInfo**              | MSALUser type |

#### MSALUser type
| Parameter     | type | 
| ---------------------- | :---: |
| **username**                  | String | 
| **userIdentifier**              | String |
| **environment**              | String |
| **tenantId**              | String |


### Acquire token silently
```
    acquireTokenSilentAsync: (scopes: string[], userIdentifier: string) => Promise<MSALResponse>;
```
```javascript
import React from 'react';
import { View, Text } from 'react-native';
import MSAL from 'react-native-msal-module';

export class App extends React.Component {
    async componentDidMount() {
        // Acquire token silent can be executed only after successful initalization of MSAL
        // Pass userId retrieved from aqcuireToken and scopes
        const msalResponse = await MSAL.acquireTokenSilentAsync(scopes, userId);
    }
    render() {
       <View>
            <Text>App</Text> 
       </View>
    }
}
```

---
### Example

In project you can find sample implementation for both platforms(iOS and Android). Example is built with RN v0.61.5. **Example won't work** without changes in config files.

### Known issues

- ~~idToken is currently unavailable in Android https://github.com/AzureAD/microsoft-authentication-library-for-android/issues/850~~
- Sometimes webview in iOS has refreshing loop

### Roadmap
It's unclear when official plugin will be released so for now:
- [x] ~~Test on newest React Native version~~
- [ ] Write tests
- [x] ~~Add example~~ 
- [ ] Implement methods for [Single Account](https://docs.microsoft.com/pl-pl/azure/active-directory/develop/single-multi-account#single-account-scenario)

### License
- See [LICENSE](https://github.com/MobileReality/react-native-msal-module/blob/master/LICENSE)
