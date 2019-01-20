package de.htw.ai.os.storage;

import java.util.Collection;

import de.htw.ai.os.bean.LocationEntry;

public interface Dao {
	
    boolean authenticate(long userId);
    
    Collection<LocationEntry> getAllLocationEntries();
    
    long addUser(String user);

	String updatePosition(LocationEntry locationTemplate, long auth);
}
