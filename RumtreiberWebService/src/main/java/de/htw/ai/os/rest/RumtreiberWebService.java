package de.htw.ai.os.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.util.JSONPObject;

import de.htw.ai.os.bean.LocationEntry;
import de.htw.ai.os.storage.Dao;
import de.htw.ai.os.storage.DaoDB;
import de.htw.ai.os.storage.TestDB;


@Path("/tunichtgut")
public class RumtreiberWebService {
	
	private Dao dao = new DaoDB();
	private static Dao testDao = new TestDB();
	
	
	@GET
	@Produces(TEXT_PLAIN) 
	public long registrateNewUser(
			@QueryParam("userName") String userName	
	){
		if (userName == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		ArrayList<LocationEntry> res = (ArrayList<LocationEntry>) dao.getAllLocationEntries();
		for (LocationEntry locationEntry : res) {
			if(locationEntry.getName().equals(userName)) {
				throw new ClientErrorException(Status.FORBIDDEN);
			} 
		}
		return dao.addUser(userName);
	} 

	@GET
	@Path("locations")
	@Produces(APPLICATION_JSON)
	public Collection<LocationEntry> getAllLocations(
			@HeaderParam("auth") long authentiationToken
	) {
		if(dao.authenticate(authentiationToken)) {
			ArrayList<LocationEntry> res = (ArrayList<LocationEntry>) dao.getAllLocationEntries();
			ArrayList<LocationEntry> export = new ArrayList<>();
			for (LocationEntry locationEntry : res) {
				long id = locationEntry.getId();
				if (id != authentiationToken) {
					export.add(locationEntry);
				}
			}
			return export;
		} else {
			throw new ClientErrorException(Status.UNAUTHORIZED);
			
		}
	}

	
	@POST 
	@Consumes(APPLICATION_JSON)
	public void updateLocation(
			@HeaderParam("auth") long authentiationToken,
			LocationEntry le
	){
		if(le == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		if(dao.authenticate(authentiationToken)) {
			String result = dao.updatePosition(le, authentiationToken);
			System.out.println("update location: " + result);
		} else {
			throw new ClientErrorException(Status.UNAUTHORIZED);
		}
		
	}
	
	@GET
	@Path("testdb")
	@Produces(TEXT_PLAIN) 
	public long registrateNewTestUser(
			@QueryParam("userName") String userName	
	){
		if (userName == null) throw new ClientErrorException(Status.FORBIDDEN);
		ArrayList<LocationEntry> res = (ArrayList<LocationEntry>) testDao.getAllLocationEntries();
		for (LocationEntry locationEntry : res) {
			if(locationEntry.getName().equals(userName)) {
				throw new ClientErrorException(Status.FORBIDDEN);
			} 
		}
		return testDao.addUser(userName);
	} 

	@GET
	@Path("testdb/locations")
	@Produces(APPLICATION_JSON)
	public Collection<LocationEntry> getAllTestLocations(
			@HeaderParam("auth") long authentiationToken
	) {
		/*Token im Authheader prüfen. Wenn gültig, alle Einträge  - außer den des Anfragenden -  in Collection speichern
		return 401 oder Collection*/
		if(testDao.authenticate(authentiationToken)) {
			ArrayList<LocationEntry> res = (ArrayList<LocationEntry>) testDao.getAllLocationEntries();
			ArrayList<LocationEntry> export = new ArrayList<>();
			for (LocationEntry locationEntry : res) {
				long id = locationEntry.getId();
				if (id != authentiationToken) {
					export.add(locationEntry);
				}
			}
			return export;
		} else {
			throw new ClientErrorException(Status.UNAUTHORIZED);
			
		}
	}

	
	@POST 
	@Path("testdb")
	@Consumes(APPLICATION_JSON)
	public void updateTestLocation(
			@HeaderParam("auth") long authentiationToken,
			LocationEntry le
	){
		/*
		anhand des Authtkens User ermiiteln
		wenn gefunden, entsprechenden DB-Eintrag aktualisieren*/
		
		if(le == null) throw new ClientErrorException(Status.FORBIDDEN);
		if(testDao.authenticate(authentiationToken)) {
			String result = testDao.updatePosition(le, authentiationToken);
			System.out.println("update location: " + result);
		} else {
			throw new ClientErrorException(Status.UNAUTHORIZED);
		}
		
	}
	
	
	

}