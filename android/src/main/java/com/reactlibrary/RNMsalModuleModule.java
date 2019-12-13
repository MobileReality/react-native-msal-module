package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;

import android.app.Activity;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class RNMsalModuleModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static IMultipleAccountPublicClientApplication msalClient = null;
    private IAccount msalAccount = null;

    public RNMsalModuleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNMsalModule";
    }

     @ReactMethod
      public void init(String clientId) throws IOException {
         InputStream stream = reactContext.getAssets().open("msal_config.json");
         File CONFIG = parseJSONIntoFile(stream);
         PublicClientApplication.createMultipleAccountPublicClientApplication(getReactApplicationContext(), CONFIG,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication msalClientResponse) {
                        Log.w("MSAL init", "initialized completed");
                        msalClient = msalClientResponse;
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.w("MSAL init", exception);
                    }
                });

      }

      @ReactMethod
      public void acquireTokenAsync(ReadableArray scopes, final Promise promise) {
          final Activity activity = getCurrentActivity();

          if (activity == null) {
              promise.reject("NO_ACTIVITY", "no activity");
              return;
          }

          if (msalClient == null) {
              promise.reject("NO_MSAL", "no msal initialized");
              return;
          }

          String[] scopesArray = new String[scopes.size()];
          for(int i=0; i < scopes.size(); i++){
              scopesArray[i] = scopes.getString(i);
          }
          try {
              msalClient.acquireToken(activity, scopesArray, handleResult(promise));
          } catch (IllegalAccessError error) {
              promise.reject("Acquire token err", error);
          }
      }

      @ReactMethod
      public void acquireTokenSilentAsync(ReadableArray scopes, String userIdentity, final Promise promise) {
          String[] scopesArray = new String[scopes.size()];
          for(int i=0; i < scopes.size(); i++){
              scopesArray[i] = scopes.getString(i);
          }
          try {
              msalAccount = msalClient.getAccount(userIdentity);
              if(msalAccount != null){
                  String authority = msalClient.getConfiguration().getDefaultAuthority().getAuthorityURL().toString();
                  Log.w("MSAL token", "Silent executed");
                  msalClient.acquireTokenSilentAsync(scopesArray, msalAccount, authority, handleSilentResult(promise));
              }
          } catch (InterruptedException | MsalException exception) {
              Log.w("MSAL token", "Silent error");
              promise.reject(exception);
          }
      }

      @ReactMethod
      public void tokenCacheDelete(String identity, final Promise promise) {
            if(msalClient != null) {
                try {
                    msalAccount = msalClient.getAccount(identity);
                } catch (InterruptedException | MsalException e) {
                    promise.resolve(true);
                }
                if (msalAccount != null) {
                    msalClient.removeAccount(msalAccount, new IMultipleAccountPublicClientApplication.RemoveAccountCallback() {
                        @Override
                        public void onRemoved() {
                            Log.w("MSAL token del success", "Success");
                            promise.resolve(true);
                        }

                        @Override
                        public void onError(MsalException exception) {
                            Log.w("MSAL token del err", "Error");
                            promise.reject(exception);
                        }
                    });
                } else {
                    Log.w("MSAL token del err", "No account");
                    promise.resolve(true);
                }
            }

      }

      private AuthenticationCallback handleResult(final Promise promise) {
          return new AuthenticationCallback() {
              @Override
              public void onSuccess(IAuthenticationResult authenticationResult) {
                  Log.w("MSAL acquire success", "Success");
                  /* Successfully got a token, use it to call a protected resource */
                  promise.resolve(msalResultToDictionary(authenticationResult));
              }

              @Override
              public void onError(MsalException exception) {
                  Log.w("MSAL acquire error", "Error");
                  if (exception instanceof MsalClientException) {
                      promise.reject(exception);
                  } else if (exception instanceof MsalServiceException) {
                      promise.reject(exception);
                  }
                  Log.w("MSAL acquireTokenAsync", exception);
              }

              @Override
              public void onCancel() {
                  promise.reject("500", "Error on receiving access token");
              }
          };
    }

    private SilentAuthenticationCallback handleSilentResult(final Promise promise) {
        return new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.w("MSAL silent success", "Success");
                /* Successfully got a token, use it to call a protected resource */
                promise.resolve(msalResultToDictionary(authenticationResult));
            }
            @Override
            public void onError(MsalException exception) {
                Log.w("MSAL silent error", "Error");
                if (exception instanceof MsalClientException) {
                    promise.reject(exception);
                } else if (exception instanceof MsalServiceException) {
                    promise.reject(exception);
                }
                Log.w("MSAL acquireTokenAsync", exception);
            }
        };
    }

    private WritableMap msalResultToDictionary(IAuthenticationResult result) {

        WritableMap resultData = new WritableNativeMap();
        resultData.putString("accessToken", result.getAccessToken());
        resultData.putString("idToken", "");
        resultData.putString("userId", result.getAccount().getId());
        resultData.putString("expiresOn", String.format("%s", result.getExpiresOn().getTime()));
        resultData.putMap("userInfo", msalUserToDictionary(result.getAccount(), result.getTenantId()));
        return resultData;
    }

    private WritableMap msalUserToDictionary(IAccount account, String tenantId) {
        WritableMap resultData = new WritableNativeMap();
        resultData.putString("userName", account.getUsername());
        resultData.putString("userIdentifier", account.getId());
        resultData.putString("name", account.getUsername());
        resultData.putString("environment", "");
        resultData.putString("tenantId", tenantId);
        return resultData;
    }

    private File parseJSONIntoFile (InputStream in) throws IOException {
        String PREFIX = "prefix";
        String SUFFIX = ".tmp";
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        try {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }

}
