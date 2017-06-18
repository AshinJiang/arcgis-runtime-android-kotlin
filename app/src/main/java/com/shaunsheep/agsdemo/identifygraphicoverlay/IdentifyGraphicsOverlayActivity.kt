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

package com.shaunsheep.agsdemo.identifygraphicoverlay

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.shaunsheep.agsdemo.R
import java.util.concurrent.ExecutionException

class IdentifyGraphicsOverlayActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var grOverlay: GraphicsOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_identify_graphics)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the BasemapType topographic
        val mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 3.184710, -4.734690, 2)
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // set up gesture for interacting with the MapView
        val mMapViewTouchListener = MapViewTouchListener(this, mMapView!!)
        mMapView!!.setOnTouchListener(mMapViewTouchListener)

        addGraphicsOverlay()
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }

    private fun addGraphicsOverlay() {
        // create the polygon
        val polygonGeometry = PolygonBuilder(SpatialReferences.getWebMercator())
        polygonGeometry.addPoint(-20e5, 20e5)
        polygonGeometry.addPoint(20e5, 20e5)
        polygonGeometry.addPoint(20e5, -20e5)
        polygonGeometry.addPoint(-20e5, -20e5)

        // create solid line symbol
        val polygonSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null)
        // create graphic from polygon geometry and symbol
        val graphic = Graphic(polygonGeometry.toGeometry(), polygonSymbol)

        // create graphics overlay
        grOverlay = GraphicsOverlay()
        // create list of graphics
        val graphics = grOverlay!!.graphics
        // add graphic to graphics overlay
        graphics.add(graphic)
        // add graphics overlay to the MapView
        mMapView!!.graphicsOverlays.add(grOverlay)
    }

    /**
     * Override default gestures of the MapView
     */
    internal inner class MapViewTouchListener
    /**
     * Constructs a DefaultMapViewOnTouchListener with the specified Context and MapView.

     * @param context the context from which this is being created
     * *
     * @param mapView the MapView with which to interact
     */
    (context: Context, mapView: MapView) : DefaultMapViewOnTouchListener(context, mapView) {

        /**
         * Override the onSingleTapConfirmed gesture to handle tapping on the MapView
         * and detected if the Graphic was selected.
         * @param e the motion event
         * *
         * @return true if the listener has consumed the event; false otherwise
         */
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            // get the screen point where user tapped
            val screenPoint = android.graphics.Point(e!!.x.toInt(), e.y.toInt())

            // identify graphics on the graphics overlay
            val identifyGraphic = mMapView.identifyGraphicsOverlayAsync(grOverlay, screenPoint, 10.0, false, 2)

            identifyGraphic.addDoneListener {
                try {
                    val grOverlayResult = identifyGraphic.get()
                    // get the list of graphics returned by identify graphic overlay
                    val graphic = grOverlayResult.graphics
                    // get size of list in results
                    val identifyResultSize = graphic.size
                    if (!graphic.isEmpty()) {
                        // show a toast message if graphic was returned
                        Toast.makeText(applicationContext, "Tapped on $identifyResultSize Graphic", Toast.LENGTH_SHORT).show()
                    }
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                } catch (ie: ExecutionException) {
                    ie.printStackTrace()
                }
            }

            return super.onSingleTapConfirmed(e)
        }

    }
}
