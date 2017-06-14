package com.shaunsheep.agsdemo.creategeometry

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.activity_mapview.*

class CreateGeometriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_create_geometry)

        // create a map with the BasemapType topographic
        val mMap = ArcGISMap(Basemap.createTopographic())

        // set the map to be displayed in this view
        mapView.map=mMap

        // create color and symbols for drawing graphics
        val markerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.BLUE, 14f)
        val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, Color.BLUE, null)
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3f)

        // add a graphic of point, multipoint, polyline and polygon.
        val overlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(overlay)
        overlay.graphics.add(Graphic(createPolygon(), fillSymbol))
        overlay.graphics.add(Graphic(createPolyline(), lineSymbol))
        overlay.graphics.add(Graphic(createMultipoint(), markerSymbol))
        overlay.graphics.add(Graphic(createPoint(), markerSymbol))

        // use the envelope to set the map viewpoint
        mapView.setViewpointGeometryAsync(createEnvelope(), resources.getDimension(R.dimen.viewpoint_padding).toDouble())
    }

    private fun createEnvelope(): Envelope {

        //[DocRef: Name=Create Envelope, Category=Fundamentals, Topic=Geometries]
        // create an Envelope using minimum and maximum x,y coordinates and a SpatialReference
        val envelope = Envelope(-123.0, 33.5, -101.0, 48.0, SpatialReferences.getWgs84())
        //[DocRef: END]

        return envelope
    }

    private fun createPoint(): Point {
        //[DocRef: Name=Create Point, Category=Fundamentals, Topic=Geometries]
        // create a Point using x,y coordinates and a SpatialReference
        val pt = Point(34.056295, -117.195800, SpatialReferences.getWgs84())
        //[DocRef: END]

        return pt
    }

    private fun createMultipoint(): Multipoint {
        //[DocRef: Name=Create Multipoint, Category=Fundamentals, Topic=Geometries]
        // create a Multipoint from a PointCollection
        val stateCapitalsPST = PointCollection(SpatialReferences.getWgs84())
        stateCapitalsPST.add(-121.491014, 38.579065) // Sacramento, CA
        stateCapitalsPST.add(-122.891366, 47.039231) // Olympia, WA
        stateCapitalsPST.add(-123.043814, 44.93326) // Salem, OR
        stateCapitalsPST.add(-119.766999, 39.164885) // Carson City, NV
        val multipoint = Multipoint(stateCapitalsPST)
        //[DocRef: END]

        return multipoint
    }

    private fun createPolyline(): Polyline {
        //[DocRef: Name=Create Polyline, Category=Fundamentals, Topic=Geometries]
        // create a Polyline from a PointCollection
        val borderCAtoNV = PointCollection(SpatialReferences.getWgs84())
        borderCAtoNV.add(-119.992, 41.989)
        borderCAtoNV.add(-119.994, 38.994)
        borderCAtoNV.add(-114.620, 35.0)
        val polyline = Polyline(borderCAtoNV)
        //[DocRef: END]

        return polyline
    }

    private fun createPolygon(): Polygon {
        //[DocRef: Name=Create Polygon, Category=Fundamentals, Topic=Geometries]
        // create a Polygon from a PointCollection
        val coloradoCorners = PointCollection(SpatialReferences.getWgs84())
        coloradoCorners.add(-109.048, 40.998)
        coloradoCorners.add(-102.047, 40.998)
        coloradoCorners.add(-102.037, 36.989)
        coloradoCorners.add(-109.048, 36.998)
        val polygon = Polygon(coloradoCorners)
        //[DocRef: END]

        return polygon
    }
}
