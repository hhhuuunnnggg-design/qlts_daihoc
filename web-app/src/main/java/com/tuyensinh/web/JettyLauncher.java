package com.tuyensinh.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setResourceBase("src/main/webapp");
        webapp.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        server.setHandler(webapp);
        server.start();
        System.out.println("Server started at http://localhost:8080");
        server.join();
    }
}
