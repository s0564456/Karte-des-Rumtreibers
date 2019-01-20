package de.htw.ai.oi.rumtreiberapp.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.sql.Timestamp;

public class LocationEntry implements Parcelable {

	@Expose
	private String name;
	@Expose
	private Timestamp lastTimestamp;
	@Expose
	private Timestamp secondLastTimestamp;
	@Expose
	private double lastLongitude;
	@Expose
	private double secondLastLongitude;
	@Expose
	private double lastLatitude;
	@Expose
	private double secondLastLatitude;
	@Expose
	private float lastDirection;
	@Expose
	private float secondLastDirection;


	private static final String TAG =
			LocationEntry.class.getSimpleName();

	//  ---------------   ----- Serialization in Android ----------------------------------------------------
	public static final Creator<LocationEntry> CREATOR = new Creator<LocationEntry>(){

		@Override
		public LocationEntry createFromParcel( final Parcel in){
			return new LocationEntry(in);
		}

		@Override
		public LocationEntry[] newArray(final int size){
			return new LocationEntry[size];
		}
	};

	private LocationEntry(Parcel in){
		readFromParcel(in);
	}

	public void readFromParcel(final Parcel in){
		name = in.readString();
		lastTimestamp = new Timestamp(in.readLong());
		secondLastTimestamp = new Timestamp(in.readLong());
		lastLongitude = in.readDouble();
		secondLastLongitude = in.readDouble();
		lastLatitude = in.readDouble();
		secondLastLatitude = in.readDouble();
		lastDirection = in.readFloat();
		secondLastDirection = in.readFloat();
		Log.d(TAG, "LocationEntry->readFromParcel(): unmarshalling: erstellt wurde " + this.getName());
	}

	/**
	 * Describe the kinds of special objects contained in this Parcelable
	 * instance's marshaled representation. For example, if the object will
	 * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
	 * the return value of this method must include the
	 * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
	 *
	 * @return a bitmask indicating the set of special object types marshaled
	 * by this Parcelable object instance.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, int i){
		out.writeString(name);
		out.writeLong(lastTimestamp.getTime());
		out.writeLong(secondLastTimestamp.getTime());
		out.writeDouble(lastLongitude);
		out.writeDouble(secondLastLongitude);
		out.writeDouble(lastLatitude);
		out.writeDouble(secondLastLatitude);
		out.writeFloat(lastDirection);
		out.writeFloat(secondLastDirection);
	}


	// ------------------ Getter and Setter -----------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Timestamp lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public Timestamp getSecondLastTimestamp() {
		return secondLastTimestamp;
	}

	public void setSecondLastTimestamp(Timestamp secondLastTimestamp) {
		this.secondLastTimestamp = secondLastTimestamp;
	}

	public double getLastLongitude() {
		return lastLongitude;
	}

	public void setLastLongitude(double lastLongitude) {
		this.lastLongitude = lastLongitude;
	}

	public double getSecondLastLongitude() {
		return secondLastLongitude;
	}

	public void setSecondLastLongitude(double secondLastLongitude) {
		this.secondLastLongitude = secondLastLongitude;
	}

	public double getLastLatitude() {
		return lastLatitude;
	}

	public void setLastLatitude(double lastLatitude) {
		this.lastLatitude = lastLatitude;
	}

	public double getSecondLastLatitude() {
		return secondLastLatitude;
	}

	public void setSecondLastLatitude(double secondLastLatitude) {
		this.secondLastLatitude = secondLastLatitude;
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

	public void setSecondLastDirection(float secondLastDirection) {
		this.secondLastDirection = secondLastDirection;
	}


	// ---------------------------- Constructors ------------------------------------------------------------------------

	public LocationEntry(){
		
	}
	
	public LocationEntry(String name, Timestamp lastTimestamp){
		this.name = name;
		this.lastTimestamp = lastTimestamp;
	}



}
