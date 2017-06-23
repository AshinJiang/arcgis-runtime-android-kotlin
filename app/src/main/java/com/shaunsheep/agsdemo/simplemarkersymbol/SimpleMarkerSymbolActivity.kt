/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.shaunsheep.agsdemo.simplemarkersymbol

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.shaunsheep.agsdemo.R


class SimpleMarkerSymbolActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_simple_marker_symbol)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the imagery basemap
        val map = ArcGISMap(Basemap.createImagery())

        // create an initial viewpoint with a point and scale
        val point = Point(-226773.0, 6550477.0, SpatialReferences.getWebMercator())
        val vp = Viewpoint(point, 7500.0)

        // set initial map extent
        map.initialViewpoint = vp

        // set the map to be displayed in the mapview
        mMapView!!.map = map

        // create a new graphics overlay and add it to the mapview
        val graphicsOverlay = GraphicsOverlay()
        mMapView!!.graphicsOverlays.add(graphicsOverlay)

        //[DocRef: Name=Point graphic with symbol, Category=Fundamentals, Topic=Symbols and Renderers]
        //create a simple marker symbol
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 12f) //size 12, style of circle

        //add a new graphic with a new point geometry
        val graphicPoint = Point(-226773.0, 6550477.0, SpatialReferences.getWebMercator())
        val graphic = Graphic(graphicPoint, symbol)
        graphicsOverlay.graphics.add(graphic)
        //[DocRef: END]

    }

    override fun onPause() {
        super.onPause()
        // pause MapView
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        // resume MapView
        mMapView!!.resume()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
