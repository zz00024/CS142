public class PlaceInformation {
   private String name;
   private String address;
   private String tag;
   //private double theLatitude;
   //private double theLongitude;
   private GeoLocation localData;
          
   public PlaceInformation(String name, String address, String tag, double latitude, double longitude) {
      this.name = name;
      this.address = address;
      this.tag = tag;
      this.localData = new GeoLocation(latitude, longitude);
      //GeoLocation localData = new GeoLocation(0,0);      
   }
      
   public String getName() {
      return name;
   }
   
   public String getAddress() {
      return address;
   }
   
   public String getTag() {
      return tag;
   }
   
   public String toString() {
      return localData.toString();
   }
   
   public GeoLocation getLocation() {
      return localData;
   }
   
   public double distanceFrom(GeoLocation spot) {
      return localData.distanceFrom(spot);
   } 
}