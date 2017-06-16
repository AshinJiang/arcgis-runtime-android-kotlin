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

package com.shaunsheep.agsdemo.featurelayerselection

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class FeatureLayerSelectionActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_feature_layer_selection)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the streets basemap
        val map = ArcGISMap(Basemap.createStreets())
        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Envelope(-1131596.019761, 3893114.069099, 3926705.982140, 7977912.461790, SpatialReferences.getWebMercator()))
        // set the map to be displayed in the MapView
        mMapView!!.map = map

        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))
        // create the feature layer using the service feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)
        featureLayer.selectionColor = Color.YELLOW
        featureLayer.selectionWidth = 10.0
        // add the layer to the map
        map.operationalLayers.add(featureLayer)

        // set an on touch listener to listen for click events
        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                // get the point that was clicked and convert it to a point in map coordinates
                val clickPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e!!.x), Math.round(e.y)))
                val tolerance = 10
                val mapTolerance = tolerance * mMapView.unitsPerDensityIndependentPixel
                // create objects required to do a selection with a query
                val envelope = Envelope(clickPoint.x - mapTolerance, clickPoint.y - mapTolerance, clickPoint.x + mapTolerance, clickPoint.y + mapTolerance, map.spatialReference)
                val query = QueryParameters()
                query.geometry = envelope
                // call select features
                val future = featureLayer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW)
                // add done loading listener to fire when the selection returns
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
                            Log.d(resources.getString(R.string.app_name), "Selection #: " + counter + " Table name: " + feature.featureTable.tableName)
                        }
                        Toast.makeText(applicationContext, counter.toString() + " features selected", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(resources.getString(R.string.app_name), "Select feature failed: " + e.message)
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
        })
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
