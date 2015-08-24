package com.intuit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/*
 * This class is a controller for the homepage
 */
@Controller
public class HomeController {

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index(HttpServletRequest request) {
        //If a session does not exist, force user to update configurations and start session
        if (request.getSession(false) == null) {
            return "config";
        } else {
            return "home";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/config")
    public String config() {
        return "config";
    }

    @RequestMapping(value = "/logout.htm", method = RequestMethod.GET)
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}

