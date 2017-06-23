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

package com.shaunsheep.agsdemo.setmapinitialextent

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class SetMapInitialExtentActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_set_init_map_area)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create new Tiled Layer from service url
        val mTopoBasemap = ArcGISTiledLayer(resources.getString(R.string.world_topo_service))
        // set tiled layer as basemap
        val mBasemap = Basemap(mTopoBasemap)
        // create a map with the basemap
        val mMap = ArcGISMap(mBasemap)

        // create an initial extent envelope
        val mInitExtent = Envelope(-12211308.778729, 4645116.003309, -12208257.879667, 4650542.535773, SpatialReference.create(102100))
        // create a viewpoint from envelope
        val vp = Viewpoint(mInitExtent)
        // set initial map extent
        mMap.initialViewpoint = vp

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
