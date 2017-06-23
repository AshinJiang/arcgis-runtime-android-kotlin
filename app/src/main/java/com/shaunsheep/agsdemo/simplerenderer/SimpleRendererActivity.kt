/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.shaunsheep.agsdemo.simplerenderer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.shaunsheep.agsdemo.R


class SimpleRendererActivity : AppCompatActivity() {

    private var mMapView: MapView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_simple_renderer)

        //Create points to add graphics to the map to allow a renderer to style them
        //These are in WGS84 coordinates (Long, Lat)
        val oldFaithfullPoint = Point(-110.828140, 44.460458, SpatialReferences.getWgs84())
        val cascadeGeyserPoint = Point(-110.829004, 44.462438, SpatialReferences.getWgs84())
        val plumeGeyserPoint = Point(-110.829381, 44.462735, SpatialReferences.getWgs84())
        //Use the farthest points to create an envelope to use for the map views visible area
        val initialEnvelope = Envelope(oldFaithfullPoint, plumeGeyserPoint)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the imagery basemap. This will set the map to have a WebMercator spatial reference
        val map = ArcGISMap(Basemap.createImageryWithLabels())
        // set the map to be displayed in the mapview
        mMapView!!.map = map

        //set initial envelope on the map view sith some padding so all points will be visible
        //This envelope is using the WGS84 points above, but is reprojected by the mapview into the maps spatial reference, so its works fine
        mMapView!!.setViewpointGeometryAsync(initialEnvelope, 100.0)

        // create a new graphics overlay and add it to the mapview
        val graphicOverlay = GraphicsOverlay()
        mMapView!!.graphicsOverlays.add(graphicOverlay)

        //[DocRef: Name=Simple Renderer, Category=Fundamentals, Topic=Symbols and Renderers]
        //create a simple symbol for use in a simple renderer
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 12f) //size 12, style of cross
        val renderer = SimpleRenderer(symbol)

        //apply the renderer to the graphics overlay (so all graphics will use the same symbol from the renderer)
        graphicOverlay.renderer = renderer
        //[DocRef: END]

        //create graphics from the geyser location points. NOTE: no need to set the symbol on the graphic because the renderer takes care of it
        //The points are in WGS84, but graphics get reprojected automatically, so they work fine in a map with a spatial reference of web mercator
        val oldFaithfullGraphic = Graphic(oldFaithfullPoint)
        val cascadeGeyserGraphic = Graphic(cascadeGeyserPoint)
        val plumeGeyserGraphic = Graphic(plumeGeyserPoint)
        graphicOverlay.graphics.add(oldFaithfullGraphic)
        graphicOverlay.graphics.add(cascadeGeyserGraphic)
        graphicOverlay.graphics.add(plumeGeyserGraphic)

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
