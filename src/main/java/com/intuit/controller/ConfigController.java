package com.intuit.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/*
 * This class is a controller for updating the configurations on the Config Tab
 */
@Controller
public class ConfigController {

    @RequestMapping(value = "/changetokens", method = RequestMethod.POST)
    public String updateTokens(final HttpServletRequest request, @RequestParam("ConsumerKey") String consumerKey,
                               @RequestParam("ConsumerSec") String consumerSec,
                               @RequestParam("AppToken") String appToken, @RequestParam("AppURL") String appURL,
                               @RequestParam("OAuthURL") String oAuthURL,
                               @RequestParam("AppcenterURL") String appcenterURL,
                               @RequestParam("OpenIDURL") String openIDURL,
                               @RequestParam("DisconnectURL") String disconnectURL,
                               @RequestParam("ReconnectURL") String reconnectURL,
                               @RequestParam("QBURL") String queryURL, @RequestParam("JSVersion") String jSVersion) {

        final HttpSession session;

        //If a session does not exist yet, start the session
        if (request.getSession(false) == null) {
            initializeSession(request);
        }

        session = request.getSession(false);

        //update session attributes
        session.setAttribute("oauth_consumer_key", consumerKey);
        session.setAttribute("oauth_consumer_sec", consumerSec);
        session.setAttribute("apptoken", appToken);
        session.setAttribute("app_url", appURL);
        session.setAttribute("oauth_url", oAuthURL);
        session.setAttribute("appcenter_url", appcenterURL);
        session.setAttribute("openid_url", openIDURL);
        session.setAttribute("disconnect_url", disconnectURL);
        session.setAttribute("reconnect_url", reconnectURL);
        session.setAttribute("query_url", queryURL);
        session.setAttribute("jsversion", jSVersion);

        return "redirect:/config";
    }

    /*
     * This method is called when the user updates teh OAuth Options
     */
    @RequestMapping(value = "/changeoptions", method = RequestMethod.POST)
    public String updateOAuthOptions(final HttpServletRequest request,
                                     @RequestParam("OAuthOptions") String OAuthOptions) {
        if (request.getSession(false) != null) {
            HttpSession session = request.getSession(false);
            session.setAttribute("oauth_options", OAuthOptions);
        }
        return "redirect:/config";
    }

    @RequestMapping(value = "/prodautoconfig", method = RequestMethod.GET)
    public String prodAutoConfig(final HttpServletRequest request) {

        final HttpSession session;
        if (request.getSession(false) == null) {
            initializeSession(request);
        }
        session = request.getSession(false);

        session.setAttribute("oauth_url", "https://oauth.intuit.com");
        session.setAttribute("appcenter_url", "https://appcenter.intuit.com");
        session.setAttribute("openid_url", "https://openid.intuit.com/OpenId/Provider");
        session.setAttribute("disconnect_url", "https://appcenter.intuit.com/api/v1/connection/disconnect");
        session.setAttribute("reconnect_url", "https://appcenter.intuit.com/api/v1/connection/reconnect");
        session.setAttribute("query_url", "https://quickbooks.api.intuit.com/v3/company/{realmid}/query?query=SELECT%20%2A%20FROM%20CompanyInfo");
        if (session.getAttribute("jsversion") == null) {
            session.setAttribute("jsversion", "https://js.appcenter.intuit.com/Content/IA/intuit.ipp.anywhere.js");
        }
        if (session.getAttribute("app_url") == null) {
            session.setAttribute("app_url", request.getRequestURL().toString().replace("/prodautoconfig", ""));
        }

        return "redirect:/config";
    }

    @RequestMapping(value = "/devautoconfig", method = RequestMethod.GET)
    public String devAutoConfig(final HttpServletRequest request) {

        final HttpSession session;
        if (request.getSession(false) == null) {
            initializeSession(request);
        }
        session = request.getSession(false);

        session.setAttribute("oauth_url", "https://oauth.intuit.com");
        session.setAttribute("appcenter_url", "https://appcenter.intuit.com");
        session.setAttribute("openid_url", "https://openid.intuit.com/OpenId/Provider");
        session.setAttribute("disconnect_url", "https://appcenter.intuit.com/api/v1/connection/disconnect");
        session.setAttribute("reconnect_url", "https://appcenter.intuit.com/api/v1/connection/reconnect");
        session.setAttribute("query_url", "https://sandbox-quickbooks.api.intuit.com/v3/company/{realmid}/query?query=SELECT%20%2A%20FROM%20CompanyInfo");
        if (session.getAttribute("jsversion") == null) {
            session.setAttribute("jsversion", "https://js.appcenter.intuit.com/Content/IA/intuit.ipp.anywhere.js");
        }
        if (session.getAttribute("app_url") == null) {
            session.setAttribute("app_url", request.getRequestURL().toString().replace("/devautoconfig", ""));
        }

        return "redirect:/config";
    }


    private void initializeSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        //update the current url
        String url = request.getRequestURL().toString();
        String subUrl = url.substring(StringUtils.ordinalIndexOf(url, "/", 3));
        url = url.replace(subUrl, "/request_token");

        //set the initial OAuth Options
        session.setAttribute("oauth_options", "intuit.ipp.anywhere.setup ({\n" +
                        "        grantUrl:\"" + url + "\",\n" +
                        "        subscribeURL: null,\n" +
                        "        datasource: {\n" +
                        "            quickbooks: true,\n" +
                        "            payments: false\n" +
                        "        },\n" +
                        "        paymentOptions: {\n" +
                        "            intuitReferred: true\n" +
                        "        }\n" +
                        "    });"
        );
    }


}
