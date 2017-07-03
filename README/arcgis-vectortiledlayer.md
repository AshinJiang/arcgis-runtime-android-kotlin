# ArcGIS Vector Tiled Layer URL

![ArcGIS VectorTiledLayer](arcgis-vectortiledlayer.png)

The ArcGIS Vector Tiled Layer URL app uses Vector Tiled Layer basemap from an ArcGIS Online service URL.
It shows how to inflate a `MapView` in the layout XML of the activity, creates a `ArcGISVectorTiledLayer` from an ArcGIS Online service URL and bind that to a ```Basemap```.  The ```Basemap``` is used to create a ```Map``` which is used inside of the ```MapView```. It uses Android Navigation drawer to switch between different Vector Tiled Layers. By default, this map supports basic zooming and panning operations.

## Features
* MapView
* ArcGISMap
* Basemap
* ArcGISVectorTiledLayer

## Developer Pattern
### Java
```java
// inflate MapView from layout
mMapView = (MapView) findViewById(R.id.mapView);

// create new Vector Tiled Layer from service url
mVectorTiledLayer = new ArcGISVectorTiledLayer(
    getResources().getString(R.string.navigation_url));

// set tiled layer as basemap
Basemap basemap = new Basemap(mVectorTiledLayer);
// create a map with the basemap
ArcGISMap map = new ArcGISMap(basemap);
// create a viewpoint from lat, long, scale
Viewpoint vp = new Viewpoint(47.606726, -122.335564, 72223.819286);
// set initial map extent
map.setInitialViewpoint(vp);
// set the map to be displayed in this view
mMapView.setMap(map);
```

### Kotlin
```kotlin
// create new Vector Tiled Layer from service url
mVectorTiledLayer = ArcGISVectorTiledLayer(resources.getString(R.string.navigation_url))

// set tiled layer as basemap
val basemap = Basemap(mVectorTiledLayer)
// create a map with the basemap
val map = ArcGISMap(basemap)
// create a viewpoint from lat, long, scale
val vp = Viewpoint(47.606726, -122.335564, 72223.819286)
// set initial map extent
map.initialViewpoint = vp
// set the map to be displayed in this view
mMapView!!.map=map
```