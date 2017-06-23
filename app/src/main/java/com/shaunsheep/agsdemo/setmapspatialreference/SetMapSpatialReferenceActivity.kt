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

package com.shaunsheep.agsdemo.setmapspatialreference

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class SetMapSpatialReferenceActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_set_map_spatialreference)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with World_Bonne projection
        val mMap = ArcGISMap(SpatialReference.create(54024))
        //Adding a map image layer which can reproject itself to the map's spatial reference
        val mapImageLayer = ArcGISMapImageLayer(resources.getString(R.string.world_cities_service))
        // set the map image layer as basemap
        val basemap = Basemap(mapImageLayer)
        // add the basemap to the map
        mMap.basemap = basemap
        // set the map to be displayed in this view
        mMapView!!.map = mMap

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
}
