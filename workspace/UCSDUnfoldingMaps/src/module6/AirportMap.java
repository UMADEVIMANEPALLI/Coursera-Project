package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;
/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {
		// setting up PAppler
		//size(800,600, OPENGL);
		size(900, 700, OPENGL);
		
		// setting up map and default events
		//map = new UnfoldingMap(this, 50, 50, 750, 550);
		map = new UnfoldingMap(this, 200, 50, 650, 600,new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		HashMap<Integer, Integer> airports_count = new HashMap<Integer, Integer>();

		// create markers from features
		for(PointFeature feature : features) {
			feature.addProperty("routecount", 0);
			AirportMarker m = new AirportMarker(feature);
	
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			airports_count.put(Integer.parseInt(feature.getId()), 0);
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route, calculate airport total route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
				airports_count.put(source, airports_count.get(source)+1);
				//airports_count.put(dest, airports_count.get(dest)+1);
				
				//for(Marker airport: airportList){
				//	if(Integer.parseInt((String)airport.getId()) == source || Integer.parseInt((String)airport.getId()) == dest){
				//		airport.setProperty("routecount", ((int)airport.getProperty("routecount")) +1);
				//	}
				//}
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
			//System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES 
			routeList.add(sl);
		}
		
		for(Marker airport: airportList){
				airport.setProperty( "routecount" , airports_count.get( Integer.parseInt((String)airport.getId())) );
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES

		map.addMarkers(airportList);
		map.addMarkers(routeList);
		
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(airportList);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkAirportRouteforClick();
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : airportList) {
			//if(((int)((AirportMarker)marker).getProperty("routecount")) <5){
				marker.setHidden(false);
			//}
			//else{
			//	marker.setHidden(true);
		//	}
		}
		for(Marker marker : routeList) {
			marker.setHidden(false);
		}
	}
	
	private void checkAirportRouteforClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected

		for (Marker marker : airportList) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				for (Marker mhide : airportList) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				
				for (Marker mhide : routeList) {
					SimpleLinesMarker routeMarker = (SimpleLinesMarker)mhide;
					if(Integer.parseInt((String)routeMarker.getProperty("source")) != Integer.parseInt((String)lastClicked.getId()) ){
						routeMarker.setHidden(true);
					}
				}
			}
		}
        if(lastClicked==null){
        	return;
        }
        else{
		    for(Marker routemarker : routeList){
			    if(Integer.parseInt((String)routemarker.getProperty("source"))== Integer.parseInt((String)lastClicked.getId())){
				    for (Marker marker : airportList) {
				        if(Integer.parseInt((String)routemarker.getProperty("destination"))== Integer.parseInt((String)marker.getId())){
					        marker.setHidden(false);
					    }
				    }
			    }
		    }
        }
	}
	
	public void draw() {
		background(0);
		map.draw();
	    
		fill(255, 250, 240);
		
		int xbase = 10;
		int ybase = 50;
		
		rect(xbase, ybase, 180, 120);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Airport Busy Level", xbase+10, ybase+25);
		
	//	fill(150, 30, 30);
	//	int tri_xbase = xbase + 35;
	//	int tri_ybase = ybase + 50;
	//	triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
	//			tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
	//			tri_ybase+CityMarker.TRI_SIZE);

	//	fill(0, 0, 0);
	//	textAlign(LEFT, CENTER);
	//	text("City Marker", tri_xbase + 15, tri_ybase);
		
	//	text("Land Quake", xbase+50, ybase+70);
	//	text("Ocean Quake", xbase+50, ybase+90);
	//	text("Size ~ Magnitude", xbase+25, ybase+110);
		
	//	fill(255, 255, 255);
	//	ellipse(xbase+35, 
	//			ybase+70, 
	//			10, 
	//			10);
	//	rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+20, ybase+50, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+20, ybase+70, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+20, ybase+90, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Routes <= 50", xbase+35, ybase+50);
		text("50 < Routes <= 200", xbase+35, ybase+70);
		text("Routes > 200", xbase+35, ybase+90);		
	}
	

}

