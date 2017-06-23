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
package com.shaunsheep.agsdemo.servicefeaturetablenocache

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class ServiceFTNocacheActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_service_ft_nocache)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())
        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7, 4016869.78617381, SpatialReferences.getWebMercator()))


        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.poolpermits_service_url))

        //explicitly set the mode to on interaction no cache (every interaction (pan, query etc) new features will be requested
        serviceFeatureTable.featureRequestMode = ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE

        // create the feature layer using the service feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)

        // add the layer to the map
        map.operationalLayers.add(featureLayer)

        // set the map to be displayed in the mapview
        mMapView!!.map = map

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
