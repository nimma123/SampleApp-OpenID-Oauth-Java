package com.intuit.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

import com.intuit.utils.JSONFormatter;
import com.intuit.utils.OauthHelper;
import com.intuit.utils.PlatformResponse;
import com.intuit.utils.ReconnectResponse;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/*
 * This class is a controller for the application to authorize the Intuit user.
 *
 * In summary, the following occurs during the authorization process:
 * 1. The user clicks the Connect to QuickBooks button.
 * 2. An Intuit dialog pops open, asking the user to select a QuickBooks company.
 * 3. Another Intuit dialog appears, asking the user to authorize the app to access the QuickBooks company.
 * 4. Your app requests an OAuth request token from the Intuit OAuth service.
 * 5. Your app requests a valid OAuth access token from Intuit.
 * 6. Your app saves the valid OAuth access token in a persistent store (such as a database), associating the token
 * with the user ID and the CompanyID.
 */
@Controller
public class OAuthController {

    public static final Logger LOG = Logger.getLogger(OAuthController.class);

    private OauthHelper oauthhelper = null;

    @RequestMapping(method=RequestMethod.GET, value = "request_token")
    public void requestOAuthToken(final HttpServletResponse response, HttpSession session) throws IOException {

        oauthhelper = new OauthHelper(session);


            final Map<String, String> requestTokenAndSecret = oauthhelper.getRequestTokenSignPost(session);

            //Pull the values out of the map
            final String requestToken = requestTokenAndSecret.get("requestToken");
            final String requestTokenSecret =
                    requestTokenAndSecret.get("requestTokenSecret");

            //Store Request Token and Secret in Session
            session.setAttribute("requestToken", requestToken);
            session.setAttribute("requestTokenSecret", requestTokenSecret);

            LOG.info("RequestToken : " + requestToken);
            LOG.info("RequestTokenSecret : " + requestTokenSecret);

            // Retrieve the Authorize URL
            final String authURL = oauthhelper.getAuthorizeURL(requestToken, requestTokenSecret);

            // Redirect to the authorized URL page
            response.sendRedirect(authURL);

    }

    @RequestMapping(value = "/request_token_ready", method = RequestMethod.GET)
    public String requestTokenReady(final HttpServletRequest request) throws IOException {

        LOG.info("#### OAuthController ->  getAccessToken() - started ####");
        final HttpSession session = request.getSession(false);
        final String verifierCode = request.getParameter("oauth_verifier");
        final String realmID = request.getParameter("realmId");
        session.setAttribute("realmId", realmID);
        final String dataSource = request.getParameter("dataSource");
        session.setAttribute("dataSource", dataSource);

        final String requestToken = (String) session.getAttribute("requestToken");
        final String requestTokenSecret = (String) session
                .getAttribute("requestTokenSecret");

        LOG.info("verifier code:  " + verifierCode);
        LOG.info("realmID:  " + realmID);
        LOG.info("dataSource:  " + dataSource);


        final OauthHelper oauthhelper = new OauthHelper(session);
        LOG.info("before calling Access token API");
        final Map<String, String> accesstokenmap = oauthhelper.getAccessToken(verifierCode, requestToken,
                requestTokenSecret, session);
        LOG.info("after calling Access token API");


        //Get Attributes from Map and Store in Session
        session.setAttribute("accessToken", accesstokenmap.get("accessToken"));
        session.setAttribute("accessSec",
                accesstokenmap.get("accessTokenSecret"));
        session.setAttribute("connectionStatus", "authorized");

        //Redirect determined on whether user connecting via Try-Buy Flow or Connect to Quickbooks
        String redirectPage;
        if (session.getAttribute("direct_connect") != null &&
                request.getSession().getAttribute("direct_connect").equals("true")) {
            session.setAttribute("direct_connect", false);
            redirectPage = "home";
        }
        else {
            redirectPage = "close";
        }

        session.setAttribute("qbConnect", true);
        LOG.info("#### OAuthController ->  getAccessToken() - completed ####");
        return redirectPage;
    }

    /*
     * This method is called when user clicks to disconnect Quickbooks
     */
    @RequestMapping(value = "/disconnect", method = RequestMethod.GET)
    public String disconnectqb (final HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        final OauthHelper oauthhelper = new OauthHelper(session);

        PlatformResponse rr = oauthhelper.disconnectQB(session);

        //display error if reconnection unsuccessful
        if (rr != null && rr.getErrorCode() != 0) {
            String returnString = "Disconnection unsuccessful. Error Code:  " + rr.getErrorCode() + " Error Message: " +
                    rr.getErrorMessage();
            model.addAttribute("reconnect_error", returnString);
            return "home";
        }

        return "redirect:/";
    }

    /*
     * This method is called when the user clicks to reconnect Quickbooks
     */
    @RequestMapping(value="/reconnect", method = RequestMethod.GET)
    public String reconnectqb (final HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        final OauthHelper oauthHelper = new OauthHelper(session);

        ReconnectResponse rr = oauthhelper.reconnectQB(session);

        //display error if reconnection unsuccessful
        if (rr != null && rr.getErrorCode() != 0) {
            String returnString = "Reconnection unsuccessful. Error Code:  " + rr.getErrorCode() + " Error Message: " +
                    rr.getErrorMessage();
            model.addAttribute("reconnect_error", returnString);
            return "home";
        }
        else if (rr.getErrorCode() == 0) {
            session.setAttribute("accessToken", rr.getOAuthToken());
            session.setAttribute("accessSec", rr.getOAuthTokenSecret());
        }

        return "redirect:/";

    }

    /*
     * This method is called when the customer tab is selected.  A select all query is performed for customers
     * in the Quickbooks
     */
    @RequestMapping(value="/query")
    public String newAttempt (HttpSession session) {
        String realmId = (String) session.getAttribute("realmId");
        String queryURL = (String) session.getAttribute("query_url");

        //Include realmId in the query
        if (queryURL.contains("{realmid}")) {
            queryURL = queryURL.replace("{realmid}", realmId);
        }

        LOG.info("Query URL: " + queryURL);
        final OauthHelper oauthhelper = new OauthHelper(session);

        String queryJSON = oauthhelper.querySend(queryURL, session);

        //check that successfully received JSON data before casting to JSONObject
        if (queryJSON.charAt(0)=='{') {
            //Format returned JSON data and store in session
            JSONObject obj = new JSONObject(queryJSON);
            queryJSON = JSONFormatter.format(obj);
        }

        session.setAttribute("query_JSON", queryJSON);
        return "query";
    }
}

