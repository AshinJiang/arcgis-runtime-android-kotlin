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

package com.shaunsheep.agsdemo.maploaded

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class MapLoadedActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mMapLoadStatusTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_loaded_activity)
        setTitle(R.string.title_map_loaded)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // inflate TextView of the map load status from the layout
        mMapLoadStatusTextView = findViewById(R.id.mapLoadStatusResult) as TextView
        loadMap()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.map_loaded, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle menu item selection
        val itemId = item.itemId
        if (itemId == R.id.Refresh) {
            mMapLoadStatusTextView!!.text = ""
            // reload the map in the MapView
            loadMap()
        }
        return true
    }

    /**
     * add a load status change listener on the loadable Map and display the map load status
     */
    private fun loadMap() {

        //clear the current map load status of the TextView
        mMapLoadStatusTextView!!.text = ""
        // create a map with the BasemapType National Geographic
        val map = ArcGISMap(Basemap.createLightGrayCanvas())

        // Listener on change in map load status
        map.addLoadStatusChangedListener { loadStatusChangedEvent ->
            val mapLoadStatus: String
            mapLoadStatus = loadStatusChangedEvent.newLoadStatus.name
            // map load status can be any of LOADED, FAILED_TO_LOAD, NOT_LOADED or LOADED
            // set the status in the TextView accordingly
            when (mapLoadStatus) {
                "LOADING" -> {
                    mMapLoadStatusTextView!!.setText(R.string.status_loading)
                    mMapLoadStatusTextView!!.setTextColor(Color.BLUE)
                }

                "FAILED_TO_LOAD" -> {
                    mMapLoadStatusTextView!!.setText(R.string.status_loadFail)
                    mMapLoadStatusTextView!!.setTextColor(Color.RED)
                }

                "NOT_LOADED" -> {
                    mMapLoadStatusTextView!!.setText(R.string.status_notLoaded)
                    mMapLoadStatusTextView!!.setTextColor(Color.GRAY)
                }

                "LOADED" -> {
                    mMapLoadStatusTextView!!.setText(R.string.status_loaded)
                    mMapLoadStatusTextView!!.setTextColor(Color.GREEN)
                }

                else -> {
                    mMapLoadStatusTextView!!.setText(R.string.status_loadError)
                    mMapLoadStatusTextView!!.setTextColor(Color.WHITE)
                }
            }

            Log.d(TAG, mapLoadStatus)
        }
        // set the map to be displayed in this view
        mMapView!!.map = map
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
        if(keyCode==KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }

    companion object {

        private val TAG = "MapLoadStatus"
    }
}
