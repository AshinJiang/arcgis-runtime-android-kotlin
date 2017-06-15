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

package com.shaunsheep.agsdemo.displaydrawingstatus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DrawStatus
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class DisplayDrawingStatusActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_drawing_stauts_activity)
        setTitle(R.string.title_display_drawing_status)

        val progressBar = findViewById(R.id.progressBar) as ProgressBar

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the Basemap Type topographic
        val map = ArcGISMap(Basemap.createTopographic())
        // create an envelope
        val targetExtent = Envelope(-13639984.0, 4537387.0, -13606734.0, 4558866.0, SpatialReferences.getWebMercator())
        // use envelope to set initial viewpoint
        val initViewpoint = Viewpoint(targetExtent)
        // set the initial viewpoint in the map
        map.initialViewpoint = initViewpoint

        // create a feature table from a service url
        val svcFeaturetable = ServiceFeatureTable(resources.getString(R.string.service_feature_table_url))
        // create a feature layer
        val featureLayer = FeatureLayer(svcFeaturetable)
        // add feature layer to map
        map.operationalLayers.add(featureLayer)

        // set the map to be displayed in this view
        mMapView!!.map = map

        //[DocRef: Name=Monitor map drawing, Category=Work with maps, Topic=Display a map]
        mMapView!!.addDrawStatusChangedListener { drawStatusChangedEvent ->
            if (drawStatusChangedEvent.drawStatus == DrawStatus.IN_PROGRESS) {
                progressBar.visibility = View.VISIBLE
                Log.d("drawStatusChanged", "spinner visible")
            } else if (drawStatusChangedEvent.drawStatus == DrawStatus.COMPLETED) {
                progressBar.visibility = View.INVISIBLE
            }
        }
        //[DocRef: END]
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
}
