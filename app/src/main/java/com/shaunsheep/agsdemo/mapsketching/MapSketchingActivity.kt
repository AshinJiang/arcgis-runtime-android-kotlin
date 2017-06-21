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

package com.shaunsheep.agsdemo.mapsketching

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.widget.ImageButton

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class MapSketchingActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mSketchGraphicsOverlay: SketchGraphicsOverlay? = null
    private var mPointButton: ImageButton? = null
    private var mPolylineButton: ImageButton? = null
    private var mPolygonButton: ImageButton? = null
    private var mUndoButton: ImageButton? = null
    private var mRedoButton: ImageButton? = null
    private var mClearButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_sketching_activity)
        setTitle(R.string.title_map_sketch)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the Basemap Type topographic
        val mMap = ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 34.056295, -117.195800, 16)
        // set the map to be displayed in this view
        mMapView!!.map = mMap
        // Create a new SketchGraphicsOverlay with a new listener
        mSketchGraphicsOverlay = SketchGraphicsOverlay(mMapView!!, MySketchGraphicsOverlayEventListener())

        // Get references to all of the bottom action bar bottoms for highlighting and disabling/enabling
        mPointButton = findViewById(R.id.pointButton) as ImageButton
        mPolylineButton = findViewById(R.id.polylineButton) as ImageButton
        mPolygonButton = findViewById(R.id.polygonButton) as ImageButton

        // Disable the undo, redo, and clear button to start with
        mUndoButton = findViewById(R.id.undoButton) as ImageButton
        mUndoButton!!.isClickable = false
        mUndoButton!!.isEnabled = false

        mRedoButton = findViewById(R.id.redoButton) as ImageButton
        mRedoButton!!.isClickable = false
        mRedoButton!!.isEnabled = false

        mClearButton = findViewById(R.id.clearButton) as ImageButton
        mClearButton!!.isClickable = false
        mClearButton!!.isEnabled = false
    }

    override fun onPause() {
        mMapView!!.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * When the point button is clicked, show it as selected and enable point drawing mode.

     * @param v the button view
     */
    fun pointClick(v: View) {
        if (!v.isSelected) {
            v.isSelected = true
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POINT)
        } else {
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE)
        }
    }

    /**
     * When the polyline button is clicked, show it as selected and enable polyline drawing mode.

     * @param v the button view
     */
    fun polylineClick(v: View) {
        if (!v.isSelected) {
            v.isSelected = true
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POLYLINE)
        } else {
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE)
        }
    }

    /**
     * When the polygon button is clicked, show it as selected and enable polygon drawing mode.

     * @param v the button view
     */
    fun polygonClick(v: View) {
        if (!v.isSelected) {
            v.isSelected = true
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POLYGON)
        } else {
            mSketchGraphicsOverlay!!.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE)
        }
    }

    /**
     * When the undo button is clicked, undo the last event on the SketchGraphicsOverlay.

     * @param v the button view
     */
    fun undoClick(v: View) {
        mSketchGraphicsOverlay!!.undo()
    }

    /**
     * When the redo button is clicked, redo the last undone event on the SketchGraphicsOverlay.

     * @param v the button view
     */
    fun redoClick(v: View) {
        mSketchGraphicsOverlay!!.redo()
    }

    /**
     * When the clear button is clicked, clear all graphics on the SketchGraphicsOverlay.

     * @param v the button view
     */
    fun clearClick(v: View) {
        mSketchGraphicsOverlay!!.clear()
    }

    /**
     * Event listener for the SketchGraphicsOverlay that listens for state changes on the undo, redo, and
     * clear capabilities, as well as finished drawings, to control the enabled/disabled/selected state
     * of the various buttons.
     */
    private inner class MySketchGraphicsOverlayEventListener : SketchGraphicsOverlayEventListener {

        override fun onUndoStateChanged(undoEnabled: Boolean) {
            // Set the undo button's enabled/disabled state based on the event boolean
            mUndoButton!!.isEnabled = undoEnabled
            mUndoButton!!.isClickable = undoEnabled
        }

        override fun onRedoStateChanged(redoEnabled: Boolean) {
            // Set the redo button's enabled/disabled state based on the event boolean
            mRedoButton!!.isEnabled = redoEnabled
            mRedoButton!!.isClickable = redoEnabled
        }

        override fun onClearStateChanged(clearEnabled: Boolean) {
            // Set the clear button's enabled/disabled state based on the event boolean
            mClearButton!!.isEnabled = clearEnabled
            mClearButton!!.isClickable = clearEnabled
        }

        override fun onDrawingFinished() {
            // Reset the selected state of the drawing buttons when a drawing is finished
            mPointButton!!.isSelected = false
            mPolylineButton!!.isSelected = false
            mPolygonButton!!.isSelected = false
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
