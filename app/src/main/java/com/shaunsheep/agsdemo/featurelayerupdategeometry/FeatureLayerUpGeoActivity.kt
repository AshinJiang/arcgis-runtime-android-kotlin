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
package com.shaunsheep.agsdemo.featurelayerupdategeometry

import java.util.concurrent.ExecutionException
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R


class FeatureLayerUpGeoActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mFeatureLayer: FeatureLayer? = null
    private var mFeatureSelected = false
    private var mIdentifiedFeature: ArcGISFeature? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_feature_layer_u_geo)
        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the streets basemap
        val map = ArcGISMap(Basemap.createStreets())
        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Point(-100.343, 34.585, SpatialReferences.getWgs84()), 1E8)
        // set the map to be displayed in the MapView
        mMapView!!.map = map

        // create feature layer with its service feature table
        // create the service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))
        // create the feature layer using the service feature table
        mFeatureLayer = FeatureLayer(serviceFeatureTable)
        mFeatureLayer!!.selectionColor = Color.CYAN
        mFeatureLayer!!.selectionWidth = 3.0
        // add the layer to the map
        map.operationalLayers.add(mFeatureLayer)
        Toast.makeText(applicationContext, "Tap on a feature to select it", Toast.LENGTH_LONG).show()

        // set an on touch listener to listen for click events
        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                if (!mFeatureSelected) {
                    val screenCoordinate = android.graphics.Point(Math.round(e!!.x), Math.round(e.y))
                    val tolerance = 20.0
                    //Identify Layers to find features
                    val identifyFuture = mMapView.identifyLayerAsync(mFeatureLayer, screenCoordinate, tolerance, false, 1)
                    identifyFuture.addDoneListener {
                        try {
                            // call get on the future to get the result
                            val layerResult = identifyFuture.get()
                            val resultGeoElements = layerResult.elements

                            if (resultGeoElements.size > 0) {
                                if (resultGeoElements[0] is ArcGISFeature) {
                                    mIdentifiedFeature = resultGeoElements[0] as ArcGISFeature
                                    //Select the identified feature
                                    mFeatureLayer!!.selectFeature(mIdentifiedFeature!!)
                                    mFeatureSelected = true
                                    Toast.makeText(applicationContext, "Feature Selected. Tap on map to update its geometry ", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(applicationContext, "No Features Selected. Tap on a feature", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: InterruptedException) {
                            Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
                        } catch (e: ExecutionException) {
                            Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
                        }
                    }
                } else {
                    val movedPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e!!.x), Math.round(e.y)))
                    val normalizedPoint = GeometryEngine.normalizeCentralMeridian(movedPoint) as Point
                    mIdentifiedFeature!!.addDoneLoadingListener {
                        mIdentifiedFeature!!.geometry = normalizedPoint
                        val updateFuture = mFeatureLayer!!.featureTable.updateFeatureAsync(mIdentifiedFeature!!)
                        updateFuture.addDoneListener {
                            try {
                                // track the update
                                updateFuture.get()
                                // apply edits once the update has completed
                                if (updateFuture.isDone) {
                                    applyEditsToServer()
                                    mFeatureLayer!!.clearSelection()
                                    mFeatureSelected = false
                                } else {
                                    Log.e(resources.getString(R.string.app_name), "Update feature failed")
                                }
                            } catch (e: InterruptedException) {
                                Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
                            } catch (e: ExecutionException) {
                                Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
                            }
                        }
                    }
                    mIdentifiedFeature!!.loadAsync()
                }
                return super.onSingleTapConfirmed(e)
            }
        })
    }

    /**
     * Applies edits to the FeatureService
     */
    private fun applyEditsToServer() {
        val applyEditsFuture = (mFeatureLayer!!.featureTable as ServiceFeatureTable).applyEditsAsync()
        applyEditsFuture.addDoneListener {
            try {
                // get results of edit
                val featureEditResultsList = applyEditsFuture.get()
                if (!featureEditResultsList[0].hasCompletedWithErrors()) {
                    Toast.makeText(applicationContext, "Applied Geometry Edits to Server. ObjectID: " + featureEditResultsList[0].objectId, Toast.LENGTH_SHORT).show()
                }
            } catch (e: InterruptedException) {
                Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
            } catch (e: ExecutionException) {
                Log.e(resources.getString(R.string.app_name), "Update feature failed: " + e.message)
            }
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
