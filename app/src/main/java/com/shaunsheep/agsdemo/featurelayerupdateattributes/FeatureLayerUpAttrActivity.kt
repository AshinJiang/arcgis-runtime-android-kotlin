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

package com.shaunsheep.agsdemo.featurelayerupdateattributes

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class FeatureLayerUpAttrActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mCallout: Callout? = null
    private var mFeatureLayer: FeatureLayer? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var mClickPoint: android.graphics.Point? = null
    private var mServiceFeatureTable: ServiceFeatureTable? = null
    private var mSnackbarSuccess: Snackbar? = null
    private var mSnackbarFailure: Snackbar? = null
    private var mSelectedArcGISFeatureAttributeValue: String? = null
    private var mFeatureUpdated: Boolean = false
    private var mCoordinatorLayout: View? = null
    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feature_layer_u_attr_activity)
        setTitle(R.string.title_feature_layer_u_attr)

        mCoordinatorLayout = findViewById(R.id.snackbarPosition)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the streets basemap
        val map = ArcGISMap(Basemap.createStreets())

        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Point(-100.343, 34.585, SpatialReferences.getWgs84()), 1E8)

        // set the map to be displayed in the mapview
        mMapView!!.map = map

        // get callout, set content and show
        mCallout = mMapView!!.callout

        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setTitle(resources.getString(R.string.progress_title))
        mProgressDialog!!.setMessage(resources.getString(R.string.progress_message))

        // create feature layer with its service feature table
        // create the service feature table
        mServiceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))
        // create the feature layer using the service feature table
        mFeatureLayer = FeatureLayer(mServiceFeatureTable!!)

        // set the color that is applied to a selected feature.
        mFeatureLayer!!.selectionColor = Color.rgb(0, 255, 255) //cyan, fully opaque
        // set the width of selection color
        mFeatureLayer!!.selectionWidth = 3.0

        // add the layer to the map
        map.operationalLayers.add(mFeatureLayer)

        // set an on touch listener to listen for click events
        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                // get the point that was clicked and convert it to a point in map coordinates
                mClickPoint = android.graphics.Point(e!!.x.toInt(), e.y.toInt())

                // clear any previous selection
                mFeatureLayer!!.clearSelection()
                mSelectedArcGISFeature = null

                // identify the GeoElements in the given layer
                val identifyFuture = mMapView.identifyLayerAsync(mFeatureLayer, mClickPoint!!, 5.0, false, 1)

                // add done loading listener to fire when the selection returns
                identifyFuture.addDoneListener {
                    try {
                        // call get on the future to get the result
                        val layerResult = identifyFuture.get()
                        val resultGeoElements = layerResult.elements

                        if (resultGeoElements.size > 0) {
                            if (resultGeoElements[0] is ArcGISFeature) {
                                mSelectedArcGISFeature = resultGeoElements[0] as ArcGISFeature
                                // highlight the selected feature
                                mFeatureLayer!!.selectFeature(mSelectedArcGISFeature!!)
                                // show callout with the value for the attribute "typdamage" of the selected feature
                                mSelectedArcGISFeatureAttributeValue = mSelectedArcGISFeature!!.attributes["typdamage"] as String
                                showCallout(mSelectedArcGISFeatureAttributeValue!!)
                                Toast.makeText(applicationContext, "Tap on the info button to change attribute value", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // none of the features on the map were selected
                            mCallout!!.dismiss()
                        }
                    } catch (e: Exception) {
                        Log.e(resources.getString(R.string.app_name), "Select feature failed: " + e.message)
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
        })

        mSnackbarSuccess = Snackbar
                .make(mCoordinatorLayout!!, "Feature successfully updated", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    val snackBarText = if (updateAttributes(mSelectedArcGISFeatureAttributeValue!!)) "Feature is restored!" else "Feature restore failed!"
                    val snackbar1 = Snackbar.make(mCoordinatorLayout!!, snackBarText, Snackbar.LENGTH_SHORT)
                    snackbar1.show()
                }

        mSnackbarFailure = Snackbar
                .make(mCoordinatorLayout!!, "Feature update failed", Snackbar.LENGTH_LONG)

    }

    /**
     * Function to read the result from newly created activity

     */
    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 100) {
            // display progress dialog while updating attribute callout
            mProgressDialog!!.show()
            updateAttributes(data.getStringExtra("typdamage"))
        }
    }

    /**
     * Applies changes to the feature, Service Feature Table, and server.

     */
    private fun updateAttributes(typeDamage: String): Boolean {

        // load the selected feature
        mSelectedArcGISFeature!!.loadAsync()

        // update the selected feature
        mSelectedArcGISFeature!!.addDoneLoadingListener {
            if (mSelectedArcGISFeature!!.loadStatus == LoadStatus.FAILED_TO_LOAD) {
                Log.d(resources.getString(R.string.app_name), "Error while loading feature")
            }

            // update the Attributes map with the new selected value for "typdamage"
            mSelectedArcGISFeature!!.attributes.put("typdamage", typeDamage)

            try {
                // update feature in the feature table
                val mapViewResult = mServiceFeatureTable!!.updateFeatureAsync(mSelectedArcGISFeature!!)
                /*mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature).addDoneListener(new Runnable() {*/
                mapViewResult.addDoneListener {
                    // apply change to the server
                    val serverResult = mServiceFeatureTable!!.applyEditsAsync()

                    serverResult.addDoneListener {
                        try {

                            // check if server result successful
                            val edits = serverResult.get()
                            if (edits.size > 0) {
                                if (!edits[0].hasCompletedWithErrors()) {
                                    Log.e(resources.getString(R.string.app_name), "Feature successfully updated")
                                    mSnackbarSuccess!!.show()
                                    mFeatureUpdated = true
                                }
                            } else {
                                Log.e(resources.getString(R.string.app_name), "The attribute type was not changed")
                                mSnackbarFailure!!.show()
                                mFeatureUpdated = false
                            }
                            if (mProgressDialog!!.isShowing) {
                                mProgressDialog!!.dismiss()
                                // display the callout with the updated value
                                showCallout(mSelectedArcGISFeature!!.attributes["typdamage"] as String)
                            }

                        } catch (e: Exception) {
                            Log.e(resources.getString(R.string.app_name), "applying changes to the server failed: " + e.message)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(resources.getString(R.string.app_name), "updating feature in the feature table failed: " + e.message)
            }
        }
        return mFeatureUpdated
    }

    /**
     * Displays Callout
     * @param title the text to show in the Callout
     */
    private fun showCallout(title: String) {

        // create a text view for the callout
        val calloutLayout = RelativeLayout(applicationContext)

        val calloutContent = TextView(applicationContext)
        calloutContent.id = R.id.textview
        calloutContent.setTextColor(Color.BLACK)
        calloutContent.textSize = 18f
        calloutContent.setPadding(0, 10, 10, 0)

        calloutContent.text = title

        val relativeParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.id)

        // create image view for the callout
        val imageView = ImageView(applicationContext)
        imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.mipmap.ic_info_outline_black_18dp))
        imageView.layoutParams = relativeParams
        imageView.setOnClickListener(ImageViewOnclickListener())

        calloutLayout.addView(calloutContent)
        calloutLayout.addView(imageView)

        mCallout!!.location = mMapView!!.screenToLocation(mClickPoint!!)
        mCallout!!.content = calloutLayout
        mCallout!!.show()
    }

    /**
     * Defines the listener for the ImageView clicks
     */
    private inner class ImageViewOnclickListener : View.OnClickListener {

        override fun onClick(v: View) {
            Log.e("imageview", "tap")
            val myIntent = Intent(this@FeatureLayerUpAttrActivity, FLUPAttrDamageTypesListActivity::class.java)
            this@FeatureLayerUpAttrActivity.startActivityForResult(myIntent, 100)
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }

}