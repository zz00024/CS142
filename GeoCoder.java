// This class provides two static methods called find and findPlace that
// provide access to the Google Maps api.  They take a search string and return
// the latitude and longitude of the top hit for the search (find) along with
// metadata (findPlace).  Returns null if the search produces no results or if
// no internet connection is available.
//
// Sample calls:
//     GeoLocation location = GeoLocator.find("space needle");
//     PlaceInformation place = GeoLocator.findPlace("space needle");

import java.io.*;
import java.net.URLEncoder;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import static javax.xml.xpath.XPathConstants.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class GeoCoder {
   // URL for hitting the Google maps api
   private static final String QUERY_URL_PREFIX = 
       "https://maps.googleapis.com/maps/api/geocode/xml?sensor=false&address=";
   
    // Given a query string, returns a GeoLocation representing the coordinates
    // of Google's best guess at what the query string represents.
    //
    // Returns null if there are no results for the given query, or if we
    // cannot connect to the Maps API.
   public static GeoLocation find(String query) {
      // call google maps api, and parse results
      Document parsedLocationXML = parseQueryResultsIntoDocument(query);
      
      // bail if no results
      if (!containsLocationResult(parsedLocationXML)) {
         return null;
      }
      
      // pull geolocation from top result
      return find(parsedLocationXML);
   }
   
    // Given a query string, returns a PlaceInformation object representing the
    // coordinates and metadata of Google's best guess at what the query string
    // represents.
    //
    // Returns null if there are no results for the given query, or if we
    // cannot connect to the Maps API.
   public static PlaceInformation findPlace(String query) {
      // call google maps api, and parse results
      Document parsedLocationXML = parseQueryResultsIntoDocument(query);
      
      // bail if no results
      if (!containsLocationResult(parsedLocationXML)) {
         return null;
      }
      
      // pull pertinent information from the top result
      GeoLocation geolocation = find(parsedLocationXML);
      try {
         String name = (String) NAME_XPATH_EXP.evaluate(parsedLocationXML, STRING);
         String address = (String) ADDRESS_XPATH_EXP.evaluate(parsedLocationXML, STRING);
         String tag = getTag(parsedLocationXML);
         return new PlaceInformation(
                                     name,
                                     address,
                                     tag,
                                     geolocation.getLatitude(),
                                     geolocation.getLongitude()
                                     );
         
      } catch (XPathExpressionException xpee) {
         // Malformed or unrecognized xml
         handleException(xpee);
      }
      
      return null;
   }

   // accepts a Google Maps API XML document, returns whether it contains at
   // least one search result
   private static boolean containsLocationResult(Document parsedLocationXML) {
      try {
         String result = (String) FIRST_RESULT_XPATH_EXP.evaluate(parsedLocationXML, STRING);
         return null != result && !"".equals(result);
      } catch (XPathExpressionException xpee) {
         // Malformed or unrecognized xml
         handleException(xpee);
      }
      return false;
   }
   
   // accepts Google Maps API XML document, returns the latitude and longitude
   // of the top search result, or null if none found
   private static GeoLocation find(Document parsedLocationXML) {
      try {
         Double lat = (Double) LAT_XPATH_EXP.evaluate(parsedLocationXML, NUMBER);
         Double lng = (Double) LNG_XPATH_EXP.evaluate(parsedLocationXML, NUMBER);
         return new GeoLocation(lat, lng);
      } catch (XPathExpressionException xpee) {
         // Malformed or unrecognized xml
         handleException(xpee);
      }
      return null;
   }
   
   // accepts a Google Maps API XML document, returns a concatenation of the "type"
   // elements for the first result
   private static String getTag(Document parsedLocationXML) {
      String tag = "";
      try {
         NodeList typeNodes = (NodeList) TAG_XPATH_EXP.evaluate(parsedLocationXML, NODESET);
         if (typeNodes.getLength() >= 1) {
            tag = typeNodes.item(0).getTextContent();
            for (int i = 1; i < typeNodes.getLength(); i++) {
               tag += ", " + typeNodes.item(i).getTextContent();
            }
         }
      } catch (XPathExpressionException xpee) {
         // Malformed or unrecognized xml
         handleException(xpee);
      }
      return tag;
   }
   
   // given a search query, returns a Google Maps API document representing the search results
   private static Document parseQueryResultsIntoDocument(String query) {
      try {
         return DOC_BUILDER.parse(QUERY_URL_PREFIX + urlEncodeUTF8(query));
      } catch (SAXException saxe) {
         handleException(saxe);
      } catch (IOException ioe) {
         handleException(ioe);
      }
      
      return null;
   }
   
   // Defines how to handle any exceptions in this program -- probably can be empty.
   private static void handleException(Exception e) {
      // System.out.println(e);
   }
   
   
   
   // V--------------------------------------------------------------------------------V
   // Parsing constants and init logic:
   
   // Needed to encode the given query string to be used in the url
   private static final String CHAR_ENCODING = "UTF-8";
   
   // For parsing the XML documents that are returned by the Maps API:
   private static final DocumentBuilder DOC_BUILDER;
   
   private static final XPathExpression FIRST_RESULT_XPATH_EXP;
   private static final XPathExpression LAT_XPATH_EXP;
   private static final XPathExpression LNG_XPATH_EXP;
   private static final XPathExpression NAME_XPATH_EXP;
   private static final XPathExpression ADDRESS_XPATH_EXP;
   private static final XPathExpression TAG_XPATH_EXP;
   
   private static final String FIRST_RESULT_XPATH_PATH = "/GeocodeResponse/result[1]";
   private static final String LAT_XPATH_PATH = "/GeocodeResponse/result[1]/geometry/location/lat";
   private static final String LNG_XPATH_PATH = "/GeocodeResponse/result[1]/geometry/location/lng";
   private static final String NAME_XPATH_PATH = "/GeocodeResponse/result[1]/address_component[1]/long_name";
   private static final String ADDRESS_XPATH_PATH = "/GeocodeResponse/result[1]/formatted_address";
   private static final String TAG_XPATH_PATH = "/GeocodeResponse/result[1]/type";
   
   static {
      // create the DocumentBuilder
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      try {
         docBuilder = documentBuilderFactory.newDocumentBuilder();
      } catch (ParserConfigurationException pce) {
         handleException(pce);
      }
      
      DOC_BUILDER = docBuilder;
      
      // precompile the XPathExpressions
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      
      FIRST_RESULT_XPATH_EXP = compile(xPath, FIRST_RESULT_XPATH_PATH);
      LAT_XPATH_EXP = compile(xPath, LAT_XPATH_PATH);
      LNG_XPATH_EXP = compile(xPath, LNG_XPATH_PATH);
      NAME_XPATH_EXP = compile(xPath, NAME_XPATH_PATH);
      ADDRESS_XPATH_EXP = compile(xPath, ADDRESS_XPATH_PATH);
      TAG_XPATH_EXP = compile(xPath, TAG_XPATH_PATH);
   }
   
   // URL-encodes the given String, swallows impossible exception.
   private static String urlEncodeUTF8(String toEncode) {
      try {
         return URLEncoder.encode(toEncode, CHAR_ENCODING);
      } catch(UnsupportedEncodingException uee) {
         // Won't happen, encoding is hardcoded to a known working value.
         System.err.println("Unable to encode, charset " + CHAR_ENCODING + " is unsupported.");
         return toEncode;
      }
   }
   
   // Uses the given XPath object and xpath string to create an XPathExpression
   // (Its purpose here is to swallow any exceptions)
   private static XPathExpression compile(XPath xPath, String path) {
      try {
         return xPath.compile(path);
      } catch (XPathExpressionException xpee) {
         handleException(xpee);
      }
      return null;
   }
}
