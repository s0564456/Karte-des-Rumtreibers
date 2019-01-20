package de.htw.ai.wad.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import de.htw.ai.os.rest.RumtreiberWebService;

import javax.ws.rs.core.Application;


public class UserWebServiceTest extends JerseyTest {
			
    @Override
    protected Application configure() {
        return new ResourceConfig(RumtreiberWebService.class);
    }  
}
