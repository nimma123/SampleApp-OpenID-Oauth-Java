package com.intuit.utils;


import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;


import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.*;



/*
 * This is a utility class for OAuth routines.
 */

public class OauthHelper  {

    public static final Logger LOG = Logger.getLogger(OauthHelper.class);

    public static String REQUEST_TOKEN_URL;
    public static String ACCESS_TOKEN_URL;
    public static String AUTHORIZE_URL;
    public static String APP_URL;
    public static String CALLBACK_URL;


    public OauthHelper(HttpSession session) {
        REQUEST_TOKEN_URL = session.getAttribute("oauth_url") + "/oauth/v1/get_request_token";
        ACCESS_TOKEN_URL = session.getAttribute("oauth_url") + "/oauth/v1/get_access_token";
        AUTHORIZE_URL = session.getAttribute("appcenter_url") + "/Connect/Begin";
        APP_URL = (String) session.getAttribute("app_url");
        CALLBACK_URL = APP_URL + "/request_token_ready";
    }


    public String getAuthorizeURL(String requestToken, String requestTokenSecret) {

        String authorizeURL = "";
        try {
            authorizeURL = AUTHORIZE_URL + "?oauth_token=" + requestToken + "&oauth_callback=" + CALLBACK_URL;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }
        LOG.info("Authorize URL: " + authorizeURL);
        return authorizeURL;
    }

    public Map<String, String> getRequestTokenSignPost(HttpSession session) {

        String authURL = null;

        OAuthProvider provider = createProvider();
        String consumerKey = (String) session.getAttribute("oauth_consumer_key");
        String consumerSec = (String) session.getAttribute("oauth_consumer_sec");

        //TODO changed?
        LOG.info("Inside getRequestToken, Consumer Key and Secret: "
                + consumerKey + " " + consumerSec);
        String callback_url = CALLBACK_URL;
        LOG.info("callback URL: " + callback_url);

        OAuthConsumer oauthconsumer = new DefaultOAuthConsumer(consumerKey,
                consumerSec);

        try {
            HttpParameters additionalParams = new HttpParameters();
            additionalParams.put("oauth_callback",
                    URLEncoder.encode(callback_url, "UTF-8"));
            oauthconsumer.setAdditionalParameters(additionalParams);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage());
        }

        String requestret = "";
        String requestToken = "";
        String requestTokenSecret = "";

        try {
            String signedRequestTokenUrl = oauthconsumer
                    .sign(REQUEST_TOKEN_URL);
            LOG.info("signedRequestTokenUrl: " + signedRequestTokenUrl);

            URL url;
            url = new URL(signedRequestTokenUrl);

            HttpURLConnection httpconnection = (HttpURLConnection) url
                    .openConnection();
            httpconnection.setRequestMethod("GET");
            httpconnection
                    .setRequestProperty("Content-type", "application/xml");
            httpconnection.setRequestProperty("Content-Length", "0");
            if (httpconnection != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        httpconnection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);

                }
                rd.close();
                requestret = sb.toString();
            }
            String[] requestTokenSections = requestret.split("&");

            for (int i = 0; i < requestTokenSections.length; i++) {
                String[] currentElements = requestTokenSections[i].split("=");

                if (currentElements[0].equalsIgnoreCase("oauth_token")) {
                    requestToken = currentElements[1];
                } else if (currentElements[0]
                        .equalsIgnoreCase("oauth_token_secret")) {
                    requestTokenSecret = currentElements[1];
                }
            }

            Map<String, String> requesttokenmap = new HashMap<String, String>();

            try {
                authURL = provider.retrieveRequestToken(oauthconsumer,
                        callback_url);
            } catch (OAuthNotAuthorizedException e) {
                LOG.error(e.getLocalizedMessage());
            }
            oauthconsumer.setTokenWithSecret(oauthconsumer.getToken(),
                    oauthconsumer.getTokenSecret());

            requesttokenmap.put("requestToken", requestToken);
            requesttokenmap.put("requestTokenSecret", requestTokenSecret);
            requesttokenmap.put("authURL", authURL);
            return requesttokenmap;

        } catch (OAuthMessageSignerException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (OAuthExpectationFailedException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (OAuthCommunicationException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (MalformedURLException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
        }
        LOG.info("Error: Failed to get request token.");
        return null;

    }

    public static OAuthProvider createProvider() {
        OAuthProvider provider = new DefaultOAuthProvider(
                OauthHelper.REQUEST_TOKEN_URL, OauthHelper.ACCESS_TOKEN_URL,
                OauthHelper.AUTHORIZE_URL);

        return provider;
    }


    public Map<String, String> getAccessToken(String verifierCode, String requestToken, String requestTokenSecret,
                                              HttpSession session) {
        String consumerkey = (String) session.getAttribute("oauth_consumer_key");
        String consumersecret = (String) session.getAttribute("oauth_consumer_sec");;
        String accessToken = "";
        String accessTokenSecret = "";

        try {
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumerkey,
                    consumersecret);
            consumer.setTokenWithSecret(requestToken, requestTokenSecret);

            HttpParameters additionalParams = new HttpParameters();
            additionalParams.put("oauth_callback", "oob");
            additionalParams.put("oauth_verifier", verifierCode);
            consumer.setAdditionalParameters(additionalParams);

            String signedURL = consumer.sign(ACCESS_TOKEN_URL);
            URL url = new URL(signedURL);

            LOG.info("Signed AccessTokenRequestURL: " + url);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/xml");
            urlConnection.setRequestProperty("Content-Length", "0");

            String accesstokenresponse = "";
            if (urlConnection != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                accesstokenresponse = sb.toString();
            }
            if (accesstokenresponse != null) {
                String[] responseElements = accesstokenresponse.split("&");
                if (responseElements.length > 1) {
                    accessToken = responseElements[1].split("=")[1];
                    accessTokenSecret = responseElements[0].split("=")[1];
                    LOG.info("OAuth accessToken: " + accessToken);
                    LOG.info("OAuth accessTokenSecret: " + accessTokenSecret);
                    Map<String, String> accesstokenmap = new HashMap<String, String>();
                    accesstokenmap.put("accessToken", accessToken);
                    accesstokenmap.put("accessTokenSecret", accessTokenSecret);
                    return accesstokenmap;
                }
            }

        } catch (OAuthMessageSignerException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (OAuthExpectationFailedException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (OAuthCommunicationException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }
        return null;
    }


    public PlatformResponse disconnectQB (HttpSession session) {
        String disconnectURL = (String) session.getAttribute("disconnect_url");
        String consumer_key = (String) session.getAttribute("oauth_consumer_key");
        String consumer_secret = (String) session.getAttribute("oauth_consumer_sec");
        String access_token = (String) session.getAttribute("accessToken");
        String access_secret = (String) session.getAttribute("accessSec");

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumer_key,
                consumer_secret);

        consumer.setTokenWithSecret(access_token, access_secret);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(disconnectURL);

        try {
            consumer.sign(request);

            HttpResponse response = httpClient.execute(request);

            InputStream is = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            String xml = "";

            while ((line = reader.readLine()) != null) {
                xml += line;
            }

            LOG.info("Raw xml = " + xml);

            //Parse XML
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(PlatformResponse.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

                StringReader stringReader = new StringReader(xml);
                PlatformResponse rr = (PlatformResponse) unmarshaller.unmarshal(stringReader);

                LOG.info("ErrorMessage: " + rr.getErrorMessage());
                LOG.info("ErrorCode: " + rr.getErrorCode());
                LOG.info("ServerTime: " + rr.getServerTime());

                //Confirm disconnect succeeded
                if(rr.getErrorCode() == 0) {
                    session.setAttribute("qbConnect", false);
                }

                return rr;

            } catch (JAXBException e) {
                e.printStackTrace();
            }


        }catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
    }


        return null;
    }



    public ReconnectResponse reconnectQB (HttpSession session) {
        String reconnectURL = (String) session.getAttribute("reconnect_url");
        String consumer_key = (String) session.getAttribute("oauth_consumer_key");
        String consumer_secret = (String) session.getAttribute("oauth_consumer_sec");
        String access_token = (String) session.getAttribute("accessToken");
        String access_secret = (String) session.getAttribute("accessSec");

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumer_key,
                consumer_secret);

        consumer.setTokenWithSecret(access_token, access_secret);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(reconnectURL);

        try {
            consumer.sign(request);

            HttpResponse response = httpClient.execute(request);

            InputStream is = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            String xml = "";

            while ((line = reader.readLine()) != null) {
                xml += line;
            }

            LOG.info("Raw xml = " + xml);

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ReconnectResponse.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

                StringReader stringReader = new StringReader(xml);
                ReconnectResponse rr = (ReconnectResponse) unmarshaller.unmarshal(stringReader);

                LOG.info("ErrorMessage: " + rr.getErrorMessage());
                LOG.info("ErrorCode: " + rr.getErrorCode());
                LOG.info("ServerTime: " + rr.getServerTime());
                LOG.info("New AccessToken: " + rr.getOAuthToken());
                LOG.info("New AccessSecret: " + rr.getOAuthTokenSecret());


                return rr;

            } catch (JAXBException e) {
                e.printStackTrace();
            }



        }catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }

        return null;

    }

    public static String querySend (String queryURL, HttpSession session) {
        String consumer_key = (String) session.getAttribute("oauth_consumer_key");
        String consumer_secret = (String) session.getAttribute("oauth_consumer_sec");
        String access_token = (String) session.getAttribute("accessToken");
        String access_secret = (String) session.getAttribute("accessSec");

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumer_key,
                consumer_secret);

        consumer.setTokenWithSecret(access_token, access_secret);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(queryURL);
        request.addHeader("Accept", "application/json");

        String xml = "";
        try {
            consumer.sign(request);

            HttpResponse response = httpClient.execute(request);

            InputStream is = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;


            while ((line = reader.readLine()) != null) {
                xml += line;
            }

        }catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
        }

        return xml;
    }

}
