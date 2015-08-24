package com.intuit.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/*
 * This class is a controller for when a user directly connects to the application from Intuit Application Center
 */
@Controller
public class DirectConnectController {

    public static final Logger LOG = Logger
            .getLogger(DirectConnectController.class);

    /*
     * This method is called when the user click on the 'Try Buy' link from the
     * IntuitApplication Center.
     */
    @RequestMapping(value = "/directconnect.htm", method = RequestMethod.GET)
    public String directConnectToIntuit(final HttpServletRequest request) {
        LOG.info("DirectConnectController -> directConnectToIntuit()");

        HttpSession session = request.getSession();
        session.setAttribute("direct_connect", "true");
        session.setAttribute("qbConnect", 1);

        return "redirect:/initialize.htm";
    }

}

