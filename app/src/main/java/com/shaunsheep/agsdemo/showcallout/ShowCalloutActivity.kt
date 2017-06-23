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

package com.shaunsheep.agsdemo.showcallout

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class ShowCalloutActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mCallout: Callout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_show_callout)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the Basemap Type topographic
        val mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 10)
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {

            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                Log.d(sTag, "onSingleTapConfirmed: " + motionEvent!!.toString())

                // get the point that was clicked and convert it to a point in map coordinates
                val screenPoint = android.graphics.Point(Math.round(motionEvent.x),
                        Math.round(motionEvent.y))

                val singleTapPoint = mMapView.screenToLocation(screenPoint)

                // create a textview for the callout
                val calloutContent = TextView(applicationContext)
                calloutContent.setTextColor(Color.BLACK)
                calloutContent.setSingleLine()
                calloutContent.text = "X:" + String.format("%.2f", singleTapPoint.x)+
                        ", y:" + String.format("%.2f", singleTapPoint.y)

                // get callout, set content and show
                mCallout = mMapView.callout
                mCallout!!.location = singleTapPoint
                mCallout!!.content = calloutContent
                mCallout!!.show()

                return true
            }
        })
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

    companion object {

        private val sTag = "Gesture"
    }
}
