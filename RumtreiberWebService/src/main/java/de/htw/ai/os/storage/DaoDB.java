package de.htw.ai.os.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.postgis.PGgeometry;
import de.htw.ai.os.bean.LocationEntry;



/*
 * Datenbanktabelle Spalten:
 * integer id
 * charvar name
 * charvar password
 * timestamp without time zon last_timestemp
 * geometry last_position
 * secondlast_timestemp
 * second_last_position
 * double precision last_direction
 * second_ast direction
 * 
 */

public class DaoDB implements Dao {
	private  DataSource ds;
	private static String SELECT_ALL_LOCATIONENTRIES = "select * from ws18_19.rumtreiber";
	
	public DaoDB () {      
	        try {
	            Context ctx = new InitialContext();
	            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/rumtreiberDB");
	        } catch (NamingException e) {
	            System.out.println("NamingException getting datasource");
	            e.printStackTrace();
	        } 
	}
	 
	@Override
	public boolean authenticate(long userId) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String searchQuery = SELECT_ALL_LOCATIONENTRIES + " where id='" + userId + "'";
		
		try {
		    con = ds.getConnection();
		    stmt = con.prepareStatement(searchQuery);
		    rs = stmt.executeQuery();
		    
		    boolean more = rs.next(); 
		    if (more) { 
		    	return true; 
	    	}  
		} catch (SQLException e) {
		    System.out.println("SQLException getting user");
		    e.printStackTrace();
		} finally {
		    try {
		        if (rs != null) rs.close();
		        if (stmt != null) stmt.close();
		        if (con != null) con.close();
		    } catch (Exception e) {
		        System.out.println("Exception in closing DB resources");
		    } 
		}
		return false;
	}

	@Override
	public Collection<LocationEntry> getAllLocationEntries() {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Collection<LocationEntry> list = new ArrayList<LocationEntry>();
		LocationEntry entry = null;
		
		try {
		    con = ds.getConnection();
		    stmt = con.prepareStatement(SELECT_ALL_LOCATIONENTRIES);
		    rs = stmt.executeQuery();
		    while (rs.next()) {
		    	entry = new LocationEntry();
		    	entry.setId(rs.getInt(1));
		    	entry.setName(rs.getString(2));
		    	entry.setLastTimestamp(rs.getTimestamp(4));
		    	
		    	PGgeometry lastPosition = (PGgeometry) rs.getObject(5);
		    	if(lastPosition != null) {
		    		entry.setLastLongitude(lastPosition.getGeometry().getPoint(0).x);
			    	entry.setLastLatitude(lastPosition.getGeometry().getPoint(0).y);
		    	}
		    	
		    	entry.setSecondLastTimestamp(rs.getTimestamp(6));
		    	
		    	PGgeometry secondLastPosition = (PGgeometry) rs.getObject(7);
		    	if(secondLastPosition != null) {
		    		entry.setSecondLastLongitude(secondLastPosition.getGeometry().getPoint(0).x);
			    	entry.setSecondLastLatitude(secondLastPosition.getGeometry().getPoint(0).y);
		    	}
		    	
		    	entry.setLastDirection(rs.getFloat(8));
		    	entry.setSecondLastDirection(rs.getFloat(9));
		    	
		    	list.add(entry);
		    }
		    
		    return list;
		} catch (SQLException e) {
		    System.out.println("SQLException getting user");
		    e.printStackTrace();
		    return list;
		} finally {
		    try {
		        if (rs != null) rs.close();
		        if (stmt != null) stmt.close();
		        if (con != null) con.close();
		    } catch (Exception e) {
		        System.out.println("Exception in closing DB resources");
		    } 
		}
	}
	
	public String updatePosition(LocationEntry locationTemplate, long auth) {
		Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ds.getConnection();
            stmt = con.prepareStatement("UPDATE ws18_19.rumtreiber "
            		+ "SET last_timestemp = ?,  secondlast_timestemp = ?, "
            		+ "last_position = ST_GeomFromText('POINT(" + locationTemplate.getLastLongitude() + " " +  locationTemplate.getLastLatitude() + ")',4326), "
    				+ "secondlast_position = ST_GeomFromText('POINT(" + locationTemplate.getSecondLastLongitude() + " " +  locationTemplate.getSecondLastLatitude() + ")',4326), "
            		+ "last_direction = ?, secondlast_direction = ?"
            		+ " WHERE id = ?");
            stmt.setTimestamp(1, locationTemplate.getLastTimestamp());
            stmt.setTimestamp(2, locationTemplate.getSecondLastTimestamp());
            stmt.setFloat(3, locationTemplate.getLastDirection());
            stmt.setFloat(4, locationTemplate.getSecondLastDirection());
            stmt.setLong(5, auth);
            stmt.executeUpdate();
            return "INSERTION SUCCEDED";
        } catch (SQLException e) {
            System.out.println("SQLException updating location");
            e.printStackTrace();
            return "INSERTION FAILED";
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Exception in closing DB resources");
            } 
        }
		
	}

	@Override
	public long addUser(String user) {
		// TODO Automatisch generierter Methodenstub
		Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        //insert new user
        try {
            con = ds.getConnection();
            stmt = con.prepareStatement("INSERT INTO ws18_19.rumtreiber (name)" + 
            		"VALUES ('" + user + "'); ");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException updating location");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Exception in closing DB resources");
            } 
        }
        
        //return user id
        try {
        	con = ds.getConnection();
            stmt = con.prepareStatement(SELECT_ALL_LOCATIONENTRIES + " where name='" + user + "'");
            rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("SQLException updating location");
            e.printStackTrace();
        } finally {
            try {
            	if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Exception in closing DB resources");
            } 
        }
        
		return 0;
	}

}
