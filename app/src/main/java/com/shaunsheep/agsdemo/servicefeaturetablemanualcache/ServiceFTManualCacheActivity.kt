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

package com.shaunsheep.agsdemo.servicefeaturetablemanualcache

import java.util.ArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class ServiceFTManualCacheActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_service_FT_manual_cache)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())

        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sf311_service_url))

        //explicitly set the mode to on manual cache (which means you need to call populate from service)
        serviceFeatureTable.featureRequestMode = ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE

        // create the feature layer using the service feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)

        // add the layer to the map
        map.operationalLayers.add(featureLayer)

        // load the table
        serviceFeatureTable.loadAsync()
        // add a done loading listener to call populate from service when the table is loaded is done
        serviceFeatureTable.addDoneLoadingListener {
            // set up the query parameters
            val params = QueryParameters()
            // for a specific 311 request type
            params.whereClause = "req_type = 'Tree Maintenance or Damage'"
            // set all outfields
            val outFields = ArrayList<String>()
            outFields.add("*")
            //populate the table based on the query, listen for result in a listenable future
            val future = serviceFeatureTable.populateFromServiceAsync(params, true, outFields)
            //add done listener to the future which fires when the async method is complete
            future.addDoneListener {
                try {
                    //call get on the future to get the result
                    val result = future.get()
                    // create an Iterator
                    val iterator = result.iterator()
                    var feature: Feature
                    // cycle through selections
                    var counter = 0
                    while (iterator.hasNext()) {
                        feature = iterator.next()
                        counter++
                        Log.d(resources.getString(R.string.title_service_FT_manual_cache), "Selection #: " + counter + " Table name: " + feature.featureTable.tableName)
                    }
                    Toast.makeText(applicationContext, counter.toString() + " features returned", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Log.e(resources.getString(R.string.title_service_FT_manual_cache), "Populate from service failed: " + e.message)
                }
            }
        }


        // set the map to be displayed in the mapview
        mMapView!!.map = map

        //set a viewpoint on the mapview so it zooms to the features once they are cached.
        mMapView!!.setViewpoint(Viewpoint(Point(-13630484.0, 4545415.0, SpatialReferences.getWebMercator()), 500000.0))

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
