package com.intuit.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;

/*
 * This is a utility class for OpenID routines.
 */

public class OpenIdHelper {

    public static final Logger LOG = Logger.getLogger(OpenIdHelper.class);

    public static String OPENID_PROVIDER_URL;
    public static String OPENID_RETURN_URL;

    public OpenIdHelper (HttpSession session) {
        OPENID_PROVIDER_URL = (String) session.getAttribute("openid_url");
        OPENID_RETURN_URL = session.getAttribute("app_url") + "/verifyopenid.htm";

    }

    public String getProviderURL () {return OPENID_PROVIDER_URL;}

    public String getReturnURL () {return OPENID_RETURN_URL;}

    public Identifier verifyResponse(final HttpServletRequest httpReq) {
        LOG.info("start verifyResponse method ");
        try {
            // Extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider).
            final ParameterList response = new ParameterList(httpReq
                    .getParameterMap());

            // Retrieve the previously stored discovery information.
            final DiscoveryInformation discovered = (DiscoveryInformation) httpReq
                    .getSession().getAttribute("openid-disc");

            // Extract the receiving URL from the HTTP request.
            final StringBuffer receivingURL = httpReq.getRequestURL();
            final String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(httpReq.getQueryString());
            }

            // Verify the response.  Note that ConsumerManager needs to be the same
            // instance used to place the authentication request.
            final HttpSession session = httpReq.getSession();
            LOG.info("Session Id : " + session.getId());
            final ConsumerManager manager = (ConsumerManager) session.getAttribute("consumerManager");
            final VerificationResult verification = manager.verify(receivingURL
                    .toString(), response, discovered);

            // Examine the verification result and extract the verified
            // identifier.
            final Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                final AuthSuccess authSuccess = (AuthSuccess) verification
                        .getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);
                    // The following attributes are null.  Use the alias parameters in VerifyOpenIDServlet instead.
                    // String firstName = fetchResp.getAttributeValue("FirstName");
                    // String lastName = fetchResp.getAttributeValue("LastName");
                    // String fullName = fetchResp.getAttributeValue("FullName");
                    // String realmID = fetchResp.getAttributeValue("RealmID");

                    // List emails = fetchResp.getAttributeValues("Email");
                    // String email1 = (String) emails.get(0);
                }

                return verified; // Success.
            }
        } catch (OpenIDException e) {
            LOG.error("OpenIDException caught in verifyResponse: " + e.toString());
        }

        LOG.info("Error: OpenIDHelper.verifyResponse(), verified is null.");
        return null;
    }

}
