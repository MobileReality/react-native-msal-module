#import "RNMsalModule.h"
#import <React/RCTLog.h>
#import <MSAL/MSAL.h>
#import <React/RCTRootView.h>
#import <React/RCTBridge.h>

@implementation RNMsalModule

MSALPublicClientApplicationConfig *config;
MSALPublicClientApplication *msalClient;

NSString *accountIdentifier;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(init:(NSString *) clientId)
{

        NSError *msalError = nil;
        NSLog(@"MSAL init");
        config = [[MSALPublicClientApplicationConfig alloc] initWithClientId:clientId];
        msalClient = [[MSALPublicClientApplication alloc] initWithConfiguration:config error:&msalError];
}

RCT_REMAP_METHOD(acquireTokenAsync,
                scopes:(NSArray<NSString*>*)scopes
                getUser:(RCTPromiseResolveBlock)resolve
                rejecter:(RCTPromiseRejectBlock)reject
                )
{
    NSLog(@"MSAL acquireTokenAsync");
    UIViewController *rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    MSALWebviewParameters *webParameters = [[MSALWebviewParameters alloc] initWithParentViewController:rootViewController];
    MSALInteractiveTokenParameters *interactiveParams = [[MSALInteractiveTokenParameters alloc] initWithScopes:scopes webviewParameters:webParameters];
    [msalClient acquireTokenWithParameters:interactiveParams completionBlock:^(MSALResult *result, NSError *error) {
        if (!error)
        {
            resolve([self MSALResultToDictionary:result]);
        }
        else if(error)
        {
            reject(@"acquire_token_async_erro",@"There were problems in fetching tokens",error);
        }
    }];
}

RCT_REMAP_METHOD(acquireTokenSilentAsync,
            scopes:(NSArray<NSString*>*)scopes
             homeAccountIdentifier:(NSString*)homeAccountIdentifier
            resolver:(RCTPromiseResolveBlock)resolve
            rejecter:(RCTPromiseRejectBlock)reject
            )
{
    NSLog(@"MSAL acquireTokenSilentAsync");
    NSError *error = nil;
    MSALAccount *account = [msalClient accountForIdentifier:homeAccountIdentifier error:&error];
    if (!account)
    {
        reject(@"get_user_error",@"There were no user",error);
    }
        
    MSALSilentTokenParameters *silentParams = [[MSALSilentTokenParameters alloc] initWithScopes:scopes account:account];
    [msalClient acquireTokenSilentWithParameters:silentParams completionBlock:^(MSALResult *result, NSError *error) {
        if (!error)
        {
            resolve([self MSALResultToDictionary:result]);
        }
        else
        {
            // Check the error
            if ([error.domain isEqual:MSALErrorDomain] && error.code == MSALErrorInteractionRequired)
            {
                reject(@"get_user_error",@"There were no user",error);
            }
                
            // Other errors may require trying again later, or reporting authentication problems to the user
        }
    }];
}

RCT_REMAP_METHOD(tokenCacheDelete,
                 homeAccountIdentifier:(NSString*)homeAccountIdentifier
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    NSLog(@"MSAL tokenCacheDelete");
  @try
  {
    NSError* error = nil;
 
    if (error) {
      @throw error;
    }
    
    NSArray<MSALAccount*>* accounts = [msalClient allAccounts:&error];
    
    if (error) {
      @throw error;
    }
    
    for (MSALAccount *account in accounts) {
      [msalClient removeAccount:account error:&error];
    }
    
    if (error) {
      @throw error;
    }
    
    resolve([NSNull null]);
    
  }
  @catch(NSError* error)
  {
      NSLog(@"MSAL ERROR tokenCacheDelete");
    reject([[NSString alloc] initWithFormat:@"%d", (int)error.code], error.description, error);
  }
}


- (NSDictionary*)MSALResultToDictionary:(MSALResult*)result
{
  NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
  [dict setObject:(result.accessToken ?: [NSNull null]) forKey:@"accessToken"];
  [dict setObject:(result.idToken ?: [NSNull null]) forKey:@"idToken"];
  [dict setObject:(result.account.identifier) ?: [NSNull null] forKey:@"userId"];
  [dict setObject:[NSNumber numberWithDouble:[result.expiresOn timeIntervalSince1970] * 1000] forKey:@"expiresOn"];
  [dict setObject:[self MSALUserToDictionary:result.account forTenant:result.tenantProfile.identifier] forKey:@"userInfo"];
  return [dict mutableCopy];
}

- (NSDictionary*)MSALUserToDictionary:(MSALAccount*)account
                            forTenant:(NSString*)tenantid
{
  NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
  [dict setObject:(account.username ?: [NSNull null]) forKey:@"userName"];
  [dict setObject:(account.homeAccountId.identifier ?: [NSNull null]) forKey:@"userIdentifier"];
  [dict setObject:(account.environment ?: [NSNull null]) forKey:@"environment"];
  [dict setObject:(tenantid ?: [NSNull null]) forKey:@"tenantId"];
  return [dict mutableCopy];
}
@end
