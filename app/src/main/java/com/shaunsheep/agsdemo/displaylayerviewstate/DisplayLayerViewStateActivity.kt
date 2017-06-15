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

package com.shaunsheep.agsdemo.displaylayerviewstate

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.TextView
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class DisplayLayerViewStateActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var timeZoneTextView: TextView? = null
    private var worldCensusTextView: TextView? = null
    private var recreationTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_layer_view_state_activity)
        setTitle(R.string.title_display_layer_view_state)

        // create three layers to add to the map
        val tiledLayer = ArcGISTiledLayer(application.getString(R.string.world_timezone_service_URL))
        tiledLayer.minScale = 4E8

        val imageLayer = ArcGISMapImageLayer(application.getString(R.string.world_census_service_URL))
        // setting the scales at which this layer can be viewed
        imageLayer.minScale = MIN_SCALE.toDouble()
        imageLayer.maxScale = (MIN_SCALE / 10).toDouble()

        // creating a layer from a service feature table
        val featureTable = ServiceFeatureTable(application.getString(R.string.world_facilities_service_URL))
        val featureLayer = FeatureLayer(featureTable)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the BasemapType topographic
        val mMap = ArcGISMap(Basemap.createTopographic())
        // add the layers on the map
        mMap.operationalLayers.add(tiledLayer)
        mMap.operationalLayers.add(imageLayer)
        mMap.operationalLayers.add(featureLayer)

        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // inflate TextViews from the layout
        timeZoneTextView = findViewById(R.id.worldTimeZoneStatusView) as TextView
        worldCensusTextView = findViewById(R.id.censusStatusView) as TextView
        recreationTextView = findViewById(R.id.facilitiesStatusView) as TextView

        // zoom to custom ViewPoint
        mMapView!!.setViewpoint(Viewpoint(
                Point(-11e6, 45e5, SpatialReferences.getWebMercator()), MIN_SCALE.toDouble()))

        // Listen to changes in the status of the Layer
        mMapView!!.addLayerViewStateChangedListener { layerViewStateChangedEvent ->
            // get the layer which changed it's state
            val layer = layerViewStateChangedEvent.layer

            // get the View Status of the layer
            // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, UNKNOWN
            val viewStatus = layerViewStateChangedEvent.layerViewStatus.iterator().next().toString()

            val layerIndex = mMap.operationalLayers.indexOf(layer)

            // finding and updating status of the layer
            when (layerIndex) {
                TILED_LAYER -> timeZoneTextView!!.text = viewStatusString(viewStatus)
                IMAGE_LAYER -> worldCensusTextView!!.text = viewStatusString(viewStatus)
                FEATURE_LAYER -> recreationTextView!!.text = viewStatusString(viewStatus)
            }
        }
    }

    /**
     * The method looks up the view status of the layer and returns a string which is displayed

     * @param status View Status of the layer
     * *
     * @return String equivalent of the status
     */
    private fun viewStatusString(status: String): String {

        when (status) {
            "ACTIVE" -> return application.getString(R.string.active)

            "ERROR" -> return application.getString(R.string.error)

            "LOADING" -> return application.getString(R.string.loading)

            "NOT_VISIBLE" -> return application.getString(R.string.notVisible)

            "OUT_OF_SCALE" -> return application.getString(R.string.outOfScale)
        }

        return application.getString(R.string.unknown)

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
        if (keyCode==KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }

    companion object {

        private val MIN_SCALE = 40000000
        private val TILED_LAYER = 0
        private val IMAGE_LAYER = 1
        private val FEATURE_LAYER = 2
    }
}
