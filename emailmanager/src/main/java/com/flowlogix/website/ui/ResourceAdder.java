/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.flowlogix.website.ui;

import javax.faces.application.ResourceHandler;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author lprimak
 */
@WebListener
public class ResourceAdder implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var faces = sce.getServletContext().getServletRegistration("FacesServlet");
        if (faces != null) {
            faces.addMapping(ResourceHandler.RESOURCE_IDENTIFIER + "/*");
        }
    }
}
