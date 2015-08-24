package com.intuit.utils;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Created by aslawson on 7/30/15.
 */
@XmlRootElement(name = "PlatformResponse", namespace = "http://platform.intuit.com/api/v1")
public class PlatformResponse {

    @XmlElement(name = "ErrorMessage")
    private String ErrorMessage;
    @XmlElement(name = "ErrorCode")
    private int ErrorCode;
    @XmlElement (name = "ServerTime")
    private Date ServerTime;

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


}
