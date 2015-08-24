package com.intuit.utils;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Created by aslawson on 7/30/15.
 */
@XmlRootElement(name = "ReconnectResponse", namespace = "http://platform.intuit.com/api/v1")
public class ReconnectResponse {

    @XmlElement (name = "ErrorMessage")
    String ErrorMessage;
    @XmlElement (name = "ErrorCode")
    int ErrorCode;
    @XmlElement (name = "ServerTime")
    Date ServerTime;
    String OAuthToken;
    String OAuthTokenSecret;

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String ErrorMessage){
        this.ErrorMessage = ErrorMessage;
    }

    public int getErrorCode () {
        return ErrorCode;
    }

    public void setErrorCode(int ErrorCode) {
        this.ErrorCode=ErrorCode;
    }

    public Date getServerTime() {
        return ServerTime;
    }

    public void setServerTime (Date ServerTime) {
        this.ServerTime = ServerTime;
    }

    public String getOAuthToken() {
        return OAuthToken;
    }

    @XmlElement (name = "OAuthToken")
    public void setOAuthToken (String OAuthToken) {
        this.OAuthToken = OAuthToken;
    }

    public String getOAuthTokenSecret() {
        return OAuthTokenSecret;
    }

    @XmlElement (name = "OAuthTokenSecret")
    public void setOAuthTokenSecret(String OAuthTokenSecret) {
        this.OAuthTokenSecret = OAuthTokenSecret;
    }


}
