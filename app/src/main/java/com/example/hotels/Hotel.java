package com.example.hotels;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class Hotel implements Comparable<Hotel>{

    private int id;
    private String name;
    private String address;
    private int stars;
    private String img;
    private double distance;
    private String availability;
    private double lat;
    private double lon;
    private int suitesCount;

    Hotel(JSONObject hotelObject)
    {
        try {
            id = hotelObject.getInt("id");
            name = hotelObject.getString("name");
            address = hotelObject.getString("address");
            stars = hotelObject.getInt("stars");
            distance = hotelObject.getDouble("distance");
            availability = hotelObject.getString("suites_availability").replace(":",",");
            suitesCount = availability.split(",").length;
            lat = lon = 0;
            Log.i("Creating", "Parsing is complete");
        }catch (JSONException e)
        {
            Log.e("Parse", "Error in creating hotel");
        }
    }

    public void setAdditionalInformation(JSONObject addInf)
    {
        try {
            lat = addInf.getDouble("lat");
            lon = addInf.getDouble("lon");
            img = addInf.getString("image");
        }catch (JSONException e)
        {
            Log.e("Parse", "Error in adding info");
        }
    }

    public int getSuitesCount() { return suitesCount;}

    public String getImg(){ return img; }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getStars() {
        return stars;
    }

    public double getDistance() {
        return distance;
    }

    public String getAvailability() {
        return availability;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() { return lon; }


    public String getPreviewAddress() { return address.split(",")[0]; }

    public String getShortName(){
        return name.split(" at")[0];
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;

        if (otherObject == null || this.getClass() != otherObject.getClass()) return false;

        Hotel anotherHotel = (Hotel) otherObject;
        return (this.getId() == anotherHotel.getId() && this.getName().equals(anotherHotel.getName()));
    }

    @Override
    public int compareTo(Hotel another) {
        Double distance1 = distance;
        Double distance2 = another.getDistance();
        return distance1.compareTo(distance2);
    }
}
class SuitesComparator implements Comparator<Hotel>{
    @Override

    public int compare(Hotel lhs, Hotel rhs) {
        Integer lhsSuitesCount = lhs.getSuitesCount();
        Integer rhsSuitesCount = rhs.getSuitesCount();
        return lhsSuitesCount.compareTo(rhsSuitesCount);
    }
}

