package com.shaunsheep.agsdemo.spatialoperations

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem

import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Part
import com.esri.arcgisruntime.geometry.PartCollection
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.shaunsheep.agsdemo.R

class SpatialOperationsActivity : AppCompatActivity() {

    private val inputGeometryOverlay = GraphicsOverlay()
    private val resultGeometryOverlay = GraphicsOverlay()
    private var inputPolygon1: Polygon? = null
    private var inputPolygon2: Polygon? = null

    // simple black (0xFF000000) line symbol for outlines
    private val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000.toInt(), 1f)
    private val resultFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFE91F1F.toInt(), lineSymbol)

    // The spatial operation switching menu items.
    private var noOperationMenuItem: MenuItem? = null
    private var intersectionMenuItem: MenuItem? = null
    private var unionMenuItem: MenuItem? = null
    private var differenceMenuItem: MenuItem? = null
    private var symmetricDifferenceMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_spatial_operations)

        val mapView = findViewById(R.id.mapView) as MapView

        // create ArcGISMap with topographic basemap
        val map = ArcGISMap(Basemap.createLightGrayCanvas())
        mapView.map = map

        // create graphics overlays to show the inputs and results of the spatial operation
        mapView.graphicsOverlays.add(inputGeometryOverlay)
        mapView.graphicsOverlays.add(resultGeometryOverlay)

        // create input polygons and add graphics to display these polygons in an overlay
        createPolygons()

        // center the map view on the input geometries
        val viewpointGeom = GeometryEngine.union(inputPolygon1!!, inputPolygon2!!).extent
        mapView.setViewpointGeometryAsync(viewpointGeom, 20.0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.spatial_operations, menu)

        // Get the menu items that perform spatial operations.
        noOperationMenuItem = menu.getItem(0)
        intersectionMenuItem = menu.getItem(1)
        unionMenuItem = menu.getItem(2)
        differenceMenuItem = menu.getItem(3)
        symmetricDifferenceMenuItem = menu.getItem(4)

        // set the 'no-op' menu item checked by default
        noOperationMenuItem!!.isChecked = true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle menu item selection
        val itemId = item.itemId

        // clear previous operation result
        resultGeometryOverlay.graphics.clear()

        // perform spatial operations and add results as graphics, depending on the option selected
        // if-else is used because this sample is used elsewhere as a Library module
        if (itemId == R.id.action_no_operation) {
            // no spatial operation - graphics have been cleared previously
            noOperationMenuItem!!.isChecked = true
            return true
        } else if (itemId == R.id.action_intersection) {
            intersectionMenuItem!!.isChecked = true
            showGeometry(GeometryEngine.intersection(inputPolygon1!!, inputPolygon2!!))
            return true
        } else if (itemId == R.id.action_union) {
            unionMenuItem!!.isChecked = true
            showGeometry(GeometryEngine.union(inputPolygon1!!, inputPolygon2!!))
            return true
        } else if (itemId == R.id.action_difference) {
            differenceMenuItem!!.isChecked = true
            // note that the difference method gives different results depending on the order of input geometries
            showGeometry(GeometryEngine.difference(inputPolygon1!!, inputPolygon2!!))
            return true
        } else if (itemId == R.id.action_symmetric_difference) {
            symmetricDifferenceMenuItem!!.isChecked = true
            showGeometry(GeometryEngine.symmetricDifference(inputPolygon1!!, inputPolygon2!!))
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun showGeometry(resultGeometry: Geometry) {
        // add a graphic from the result geometry, showing result in red (0xFFE91F1F)
        val resultGraphic = Graphic(resultGeometry, resultFillSymbol)
        resultGeometryOverlay.graphics.add(resultGraphic)

        // select the result to highlight it
        resultGraphic.isSelected = true
    }

    private fun createPolygons() {

        // create input polygon 1
        val pointsPoly = PointCollection(SpatialReferences.getWebMercator())
        pointsPoly.add(Point(-13160.0, 6710100.0))
        pointsPoly.add(Point(-13300.0, 6710500.0))
        pointsPoly.add(Point(-13760.0, 6710730.0))
        pointsPoly.add(Point(-14660.0, 6710000.0))
        pointsPoly.add(Point(-13960.0, 6709400.0))
        inputPolygon1 = Polygon(pointsPoly)

        // create and add a blue graphic to show input polygon 1
        var fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x990000CC.toInt(), lineSymbol)
        inputGeometryOverlay.graphics.add(Graphic(inputPolygon1!!, fillSymbol))

        // create input polygon 2 with a green (0xFF009900) symbol
        // outer ring
        val outerRingSegmentCollection = PointCollection(SpatialReferences.getWebMercator())
        outerRingSegmentCollection.add(Point(-13060.0, 6711030.0))
        outerRingSegmentCollection.add(Point(-12160.0, 6710730.0))
        outerRingSegmentCollection.add(Point(-13160.0, 6709700.0))
        outerRingSegmentCollection.add(Point(-14560.0, 6710730.0))
        outerRingSegmentCollection.add(Point(-13060.0, 6711030.0))
        val outerRing = Part(outerRingSegmentCollection)

        // inner ring
        val innerRingSegmentCollection = PointCollection(SpatialReferences.getWebMercator())
        innerRingSegmentCollection.add(Point(-13060.0, 6710910.0))
        innerRingSegmentCollection.add(Point(-12450.0, 6710660.0))
        innerRingSegmentCollection.add(Point(-13160.0, 6709900.0))
        innerRingSegmentCollection.add(Point(-14160.0, 6710630.0))
        innerRingSegmentCollection.add(Point(-13060.0, 6710910.0))
        val innerRing = Part(innerRingSegmentCollection)

        // add both parts (rings) to a part collection and create a geometry from it
        val polygonParts = PartCollection(outerRing)
        polygonParts.add(innerRing)
        inputPolygon2 = Polygon(polygonParts)

        // create and add a green graphic to show input polygon 2
        fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x99009900.toInt(), lineSymbol)
        inputGeometryOverlay.graphics.add(Graphic(inputPolygon2!!, fillSymbol))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
