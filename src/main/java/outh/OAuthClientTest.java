package outh;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Receiving access token from FB.
 * Created by tr0k on 2016-07-21.
 */
public class OAuthClientTest {
    public static void main(String[] args) throws OAuthSystemException, IOException{

        final String clientId = "";
        final String clientSecretCode = "";
        final String redirectURI = "https://localhost:8388/";

        OAuthClientRequest request = getAuthorizationRequest(clientId, redirectURI);

        //in web application we make redirection to following uri:
        System.out.println("Visit: " + request.getLocationUri() + "\nand grant permission");

        //Receive and input code from fb service
        System.out.print("Now enter the OAuth code you have received in redirect uri ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();
        System.out.println("Received code: " + code);

        request = getAccessTokenRequest(clientId, clientSecretCode, redirectURI, code);

        //create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());


        //Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
        //application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
        //Own response class is an easy way to deal with oauth providers that introduce modifications to
        //OAuth specification
        GitHubTokenResponse oAuthResponse = null;
        try {
            oAuthResponse = oAuthClient.accessToken(request, GitHubTokenResponse.class);

            System.out.println(
                    "Access Token: " + oAuthResponse.getAccessToken() + ", Expires in: " + oAuthResponse
                            .getExpiresIn());

            System.out.println("Me: " + getFacebookProfile(oAuthClient, oAuthResponse.getAccessToken()).getBody());

        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }

    }

    /**
     * Send code to request access token
     */
    private static OAuthClientRequest getAccessTokenRequest(String clientId, String clientSecretCode,
                                                            String redirectURI, String code)
            throws OAuthSystemException {
        OAuthClientRequest request;
        request = OAuthClientRequest
                .tokenProvider(OAuthProviderType.FACEBOOK)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(clientId)
                .setClientSecret(clientSecretCode)
                .setRedirectURI(redirectURI)
                .setCode(code)
                .buildBodyMessage();
        return request;
    }

    /**
     * End user authorization request
     */
    private static OAuthClientRequest getAuthorizationRequest(String clientId, String redirectURI)
            throws OAuthSystemException {
        return OAuthClientRequest
                .authorizationProvider(OAuthProviderType.FACEBOOK)
                .setClientId(clientId)
                .setRedirectURI(redirectURI)
                .buildQueryMessage();
    }

    private static OAuthResourceResponse getFacebookProfile(OAuthClient oAuthClient, String accessToken)
            throws OAuthSystemException, OAuthProblemException {
        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://graph.facebook.com/me")
                .setAccessToken(accessToken).buildQueryMessage();

        return oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
    }
}
