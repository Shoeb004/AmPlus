package com.example.useramplus;

import androidx.appcompat.view.ActionBarPolicy;
import androidx.appcompat.view.menu.MenuBuilder;

import com.example.useramplus.Model.AnimationModel;
import com.example.useramplus.Model.DriverGeoModel;
import com.example.useramplus.Model.RiderModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String RIDER_INFO_REFERENCE = "Riders";
    public static final String DRIVERS_LOCATION_REFERENCE = "DriversLocation"; // same as driver app
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static RiderModel currentUser;
    public static Set<DriverGeoModel> driversFound = new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> markerList = new HashMap<>();
//    public static HashMap<String, AnimationModel> driverLocationSubscribe = new HashMap<String, AnimationModel>(); //V18

    public static String buildWelcomeMessage() {
        if (Common.currentUser != null){
            return new StringBuilder("Welcome ")
                    .append(Common.currentUser.getFirstName())
                    .append(" ")
                    .append(Common.currentUser.getLastName())
                    .toString();
        } else
            return "";
    }

    public static String buildName(String firstName, String lastName) {
        return new StringBuilder(firstName).append(" ").append(lastName).toString();
    }



// V18


    //DECODE POLY
//    public static List<LatLng> decodePoly(String encoded) {
//        List poly = new ArrayList();
//        int index=0,len=encoded.length();
//        int lat=0,lng=0;
//        while(index < len)
//        {
//            int b,shift=0,result=0;
//            do{
//                b=encoded.charAt(index++)-63;
//                result |= (b & 0x1f) << shift;
//                shift+=5;
//
//            }while(b >= 0x20);
//            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
//            lat += dlat;
//
//            shift = 0;
//            result = 0;
//            do{
//                b = encoded.charAt(index++)-63;
//                result |= (b & 0x1f) << shift;
//                shift +=5;
//            }while(b >= 0x20);
//            int dlng = ((result & 1)!=0 ? ~(result >> 1): (result >> 1));
//            lng +=dlng;
//
//            LatLng p = new LatLng((((double)lat / 1E5)),
//                    (((double)lng/1E5)));
//            poly.add(p);
//        }
//        return poly;
//    }

    //GET BEARING
//    public static float getBearing(LatLng begin, LatLng end) {
//        //You can copy this function by link at description
//        double lat = Math.abs(begin.latitude - end.latitude);
//        double lng = Math.abs(begin.longitude - end.longitude);
//
//        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
//            return (float) (Math.toDegrees(Math.atan(lng / lat)));
//        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
//            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
//        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
//            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
//        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
//            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
//        return -1;
//    }
}
