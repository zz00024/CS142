// This class provides a sample usage of the GeoCoder.findPlace method and the
// PlaceInformation and GeoLocation classes.  It prompts the user for two
// locations and reports the distance between them.

import java.util.*;

public class DistanceFinder {
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        System.out.println("This program finds the distance between two");
        System.out.println("places using Google Maps data.");
        System.out.println();
        System.out.print("first location? ");
        PlaceInformation one = GeoCoder.findPlace(console.nextLine());
        if (one == null) {
            System.out.println("no matches for that search string.");
        } else {
            System.out.println("found at " + one);
            System.out.print("second location? ");
            PlaceInformation two = GeoCoder.findPlace(console.nextLine());
            if (two == null) {
                System.out.println("no matches for that search string.");
            } else {
                System.out.println("found at " + two);
                System.out.printf("%.3f miles apart\n", one.distanceFrom(two.getLocation()));
                showMatch(1, one);
                showMatch(2, two);
            }
        }
    }

    public static void showMatch(int number, PlaceInformation info) {
        System.out.println();
        System.out.println("Place # " + number);
        System.out.println("    name    : " + info.getName());
        System.out.println("    address : " + info.getAddress());
        System.out.println("    tags    : " + info.getTag());
        System.out.println("    location: " + info.getLocation());
    }
}
