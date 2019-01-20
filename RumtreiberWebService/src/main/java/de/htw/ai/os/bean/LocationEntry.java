package de.htw.ai.os.bean;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class LocationEntry {
	
	static private final byte[] DEFAULT_HASH = HashTools.sha256HashCode("");
	static private final Timestamp DEFAULT_TIMESTAMP = new Timestamp(0);
	
	@JsonIgnore
	private long id;
	private String name;
	@JsonIgnore
	private byte[] passwordHash;
	private Timestamp lastTimestamp;
	private Timestamp secondLastTimestamp;
	private double lastLongitude;
	private double secondLastLongitude;
	private double lastLatitude;
	private double secondLastLatitude;
	private float lastDirection;
	private float secondLastDirection;
	
	
	public LocationEntry() {
		this.id = 0;
		this.name = "newUser";
		this.passwordHash = DEFAULT_HASH;
		this.lastTimestamp = DEFAULT_TIMESTAMP;
		this.lastTimestamp = DEFAULT_TIMESTAMP;
	}
	
	public LocationEntry(long id, String name, Timestamp timestamp) {
		this.id = id;
		this.name = name;
		this.lastTimestamp = timestamp;
		this.secondLastTimestamp = timestamp;
	}
	
	
	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(final String userId) {
		this.name = userId;
	}
	
	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(final byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Timestamp getLastTimestamp() {
		if (lastTimestamp == null) {
			return DEFAULT_TIMESTAMP;
		}else {
			return lastTimestamp;
		}
	}

	public void setLastTimestamp(Timestamp lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public Timestamp getSecondLastTimestamp() {
		if (secondLastTimestamp == null) {
			return DEFAULT_TIMESTAMP;
		} else {
			return secondLastTimestamp;
		}
	}

	public void setSecondLastTimestamp(Timestamp secondLastTimestamp) {
		this.secondLastTimestamp = secondLastTimestamp;
	}

	public double getLastLongitude() {
		return lastLongitude;
	}

	public void setLastLongitude(double lastLognitude) {
		this.lastLongitude = lastLognitude;
	}

	public double getSecondLastLongitude() {
		return secondLastLongitude;
	}

	public void setSecondLastLongitude(double secondLastLognitude) {
		this.secondLastLongitude = secondLastLognitude;
	}

	public double getLastLatitude() {
		return lastLatitude;
	}

	public void setLastLatitude(double lastLattitude) {
		this.lastLatitude = lastLattitude;
	}

	public double getSecondLastLatitude() {
		return secondLastLatitude;
	}

	public void setSecondLastLatitude(double secondLastLattitude) {
		this.secondLastLatitude = secondLastLattitude;
	}

	public float getLastDirection() {
		return lastDirection;
	}

	public void setLastDirection(float lastDirection) {
		this.lastDirection = lastDirection;
	}

	public float getSecondLastDirection() {
		return secondLastDirection;
	}

	public void setSecondLastDirection(float secondlastDirection) {
		this.secondLastDirection = secondlastDirection;
	}
}