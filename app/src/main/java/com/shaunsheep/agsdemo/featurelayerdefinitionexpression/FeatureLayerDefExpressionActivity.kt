/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.shaunsheep.agsdemo.featurelayerdefinitionexpression

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.KeyEvent

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class FeatureLayerDefExpressionActivity : AppCompatActivity() {

    private var mMapView: MapView?=null
    private var mFeatureLayer: FeatureLayer?=null

    internal var applyActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feature_layer_def_exp_activity)
        setTitle(R.string.title_feature_layer_def_exp)

        // set up the bottom toolbar
        createBottomToolbar()

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())

        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.feature_layer_def_exp_service_url))

        // create the feature layer using the service feature table
        mFeatureLayer = FeatureLayer(serviceFeatureTable)

        // add the layer to the map
        map.operationalLayers.add(mFeatureLayer)

        // set the map to be displayed in the mapview
        mMapView!!.map = map

        // zoom to a view point of the USA
        mMapView!!.setViewpointCenterAsync(Point(-13630845.0, 4544861.0, SpatialReferences.getWebMercator()), 600000.0)

    }

    private fun applyDefinitionExpression() {
        // apply a definition expression on the feature layer
        // if this is called before the layer is loaded, it will be applied to the loaded layer
        mFeatureLayer!!.definitionExpression = "req_Type = 'Tree Maintenance or Damage'"
    }

    private fun resetDefinitionExpression() {
        // set the definition expression to nothing (empty string, null also works)
        mFeatureLayer!!.definitionExpression = ""
    }

    private fun createBottomToolbar() {

        val bottomToolbar = findViewById(R.id.bottomToolbar) as Toolbar
        bottomToolbar.inflateMenu(R.menu.feature_layer_def_exp)

        bottomToolbar.setOnMenuItemClickListener { item ->
            // Handle action bar item clicks
            val itemId = item.itemId
            //if statement is used because this sample is used elsewhere as a Library module
            if (itemId == R.id.action_def_exp) {
                // check the state of the menu item
                if (!applyActive) {
                    applyDefinitionExpression()
                    // change the text to reset
                    applyActive = true
                    item.setTitle(R.string.action_reset)
                } else {
                    resetDefinitionExpression()
                    // change the text to apply
                    applyActive = false
                    item.setTitle(R.string.action_def_exp)
                }
            }
            true
        }
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
        if(keyCode==KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
