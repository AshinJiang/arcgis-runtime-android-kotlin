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

package com.shaunsheep.agsdemo.manageoperationallayers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Button

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.LayerList
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class ManageOperationalLayerActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_operational_layer_activity)
        setTitle(R.string.title_manage_operationlayer)

        val imageLayerElevation: ArcGISMapImageLayer
        val imagelayerCensus: ArcGISMapImageLayer
        val selectLayers: Button

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // inflate operational layer selection button from the layout
        selectLayers = findViewById(R.id.operationallayer) as Button

        // create a map with the BasemapType topographic
        val mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 14)

        imageLayerElevation = ArcGISMapImageLayer(resources.getString(R.string.imagelayer_elevation_url))
        imagelayerCensus = ArcGISMapImageLayer(resources.getString(R.string.imagelayer_census_url))

        // get the LayerList from the Map
        operationalLayerList = mMap.operationalLayers
        // add operational layers to the Map
        operationalLayerList!!.add(imageLayerElevation)
        operationalLayerList!!.add(imagelayerCensus)

        // set the initial viewpoint on the map
        mMap.initialViewpoint = Viewpoint(Point(-133e5, 45e5, SpatialReference.create(3857)), 2e7)

        // set the map to be displayed in this view
        mMapView!!.map = mMap


        selectLayers.setOnClickListener {
            val intent = Intent(this@ManageOperationalLayerActivity, OperationalLayers::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
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
        /**
         * returns the LayerList associated with the Map

         * @return LayerList containing all the operational layers in the Map
         */
        var operationalLayerList: LayerList? = null
            private set
    }

}
