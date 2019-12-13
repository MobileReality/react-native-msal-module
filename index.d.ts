interface MSALResponse {
    accessToken: string;
    idToken: string;
    userId: string;
    expiresOn: string;
    userInfo: MSALUserInfo;
}

interface MSALUserInfo {
    username: string;
    userIdentifier: string;
    environment: string;
    tenantId: string;
}

declare const MSAL: {
    init: (clientId: string) => void;
    acquireTokenAsync: (scopes: string[]) => Promise<MSALResponse>;
    acquireTokenSilentAsync: (scopes: string[], userIdentifier: string) => Promise<MSALResponse>;
    tokenCacheDelete: (userIdentifier: string) => void;
};

export default MSAL;
