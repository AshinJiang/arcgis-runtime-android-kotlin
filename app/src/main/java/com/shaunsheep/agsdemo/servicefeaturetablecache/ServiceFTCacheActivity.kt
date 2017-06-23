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

package com.shaunsheep.agsdemo.servicefeaturetablecache

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

class ServiceFTCacheActivity : AppCompatActivity() {

    private var mMapView: MapView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_service_feature_table_cache)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the light grey canvas basemap
        val map = ArcGISMap(Basemap.createLightGrayCanvas())
        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7, 4016869.78617381, SpatialReferences.getWebMercator()))


        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.poolpermits_service_url))

        //explicitly set the mode to on interaction cache (which is also the default mode for service feature tables)
        serviceFeatureTable.featureRequestMode = ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE

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
