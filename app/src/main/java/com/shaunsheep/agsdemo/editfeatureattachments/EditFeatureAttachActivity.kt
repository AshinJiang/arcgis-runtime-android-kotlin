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

package com.shaunsheep.agsdemo.editfeatureattachments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class EditFeatureAttachActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    private var mCalloutLayout: RelativeLayout? = null
    private var mMapView: MapView? = null
    private var mMap: ArcGISMap? = null
    private var mCallout: Callout? = null
    private var mFeatureLayer: FeatureLayer? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var mClickPoint: android.graphics.Point? = null
    private var attachments: List<Attachment>? = null
    private var mSelectedArcGISFeatureAttributeValue: String? = null
    private var mAttributeID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_edit_feature_attach)
        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the streets basemap
        mMap = ArcGISMap(Basemap.Type.STREETS, 44.354388, -119.998245, 5)
        // set the map to be displayed in the mapview
        mMapView!!.map = mMap

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle(application.getString(R.string.fetching_no_attachments))
        progressDialog!!.setMessage(application.getString(R.string.wait))
        createCallout()
        // get callout, set content and show
        mCallout = mMapView!!.callout
        // create feature layer with its service feature table
        // create the service feature table
        val mServiceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))
        mServiceFeatureTable.featureRequestMode = ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE
        // create the feature layer using the service feature table
        mFeatureLayer = FeatureLayer(mServiceFeatureTable)

        // set the color that is applied to a selected feature.
        mFeatureLayer!!.selectionColor = Color.rgb(0, 255, 255) //cyan, fully opaque
        // set the width of selection color
        mFeatureLayer!!.selectionWidth = 3.0

        // add the layer to the map
        mMap!!.operationalLayers.add(mFeatureLayer)

        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                // get the point that was clicked and convert it to a point in map coordinates
                mClickPoint = android.graphics.Point(e!!.x.toInt(), e.y.toInt())

                // clear any previous selection
                mFeatureLayer!!.clearSelection()
                mSelectedArcGISFeature = null

                // identify the GeoElements in the given layer
                val futureIdentifyLayer = mMapView.identifyLayerAsync(mFeatureLayer, mClickPoint!!, 5.0, false, 1)

                // add done loading listener to fire when the selection returns
                futureIdentifyLayer.addDoneListener {
                    try {
                        // call get on the future to get the result
                        val layerResult = futureIdentifyLayer.get()

                        val resultGeoElements = layerResult.elements
                        if (resultGeoElements.size > 0) {
                            if (resultGeoElements[0] is ArcGISFeature) {
                                progressDialog!!.show()

                                mSelectedArcGISFeature = resultGeoElements[0] as ArcGISFeature
                                // highlight the selected feature
                                mFeatureLayer!!.selectFeature(mSelectedArcGISFeature!!)
                                mAttributeID = mSelectedArcGISFeature!!.attributes["objectid"].toString()
                                // get the number of attachments
                                val attachmentResults = mSelectedArcGISFeature!!.fetchAttachmentsAsync()

                                attachmentResults.addDoneListener {
                                    try {
                                        attachments = attachmentResults.get()
                                        Log.d("number of attachments :", attachments!!.size.toString() + "")
                                        // show callout with the value for the attribute "typdamage" of the selected feature
                                        mSelectedArcGISFeatureAttributeValue = mSelectedArcGISFeature!!.attributes["typdamage"] as String
                                        if (progressDialog!!.isShowing) {
                                            progressDialog!!.dismiss()
                                        }
                                        showCallout(mSelectedArcGISFeatureAttributeValue!!, attachments!!.size)
                                        Toast.makeText(applicationContext, application.getString(R.string.info_button_message), Toast.LENGTH_SHORT).show()

                                    } catch (e: Exception) {
                                        Log.e(TAG, e.message)
                                    }
                                }
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
    }

    /**
     * Display the callout
     * @param title the damage type text
     * *
     * @param noOfAttachments attachment count of the selected feature
     */
    private fun showCallout(title: String, noOfAttachments: Int) {

        val calloutContent = mCalloutLayout!!.findViewById(R.id.calloutTextView) as TextView
        calloutContent.text = title

        val calloutAttachment = mCalloutLayout!!.findViewById(R.id.attchTV) as TextView
        val attachmentText = application.getString(R.string.attachment_info_message) + noOfAttachments
        calloutAttachment.text = attachmentText

        mCallout!!.location = mMapView!!.screenToLocation(mClickPoint!!)
        mCallout!!.content = mCalloutLayout!!
        mCallout!!.show()
    }

    /**
     * Create a Layout for callout
     */
    private fun createCallout() {

        // create content text view for the callout
        mCalloutLayout = RelativeLayout(applicationContext)
        val calloutContent = TextView(applicationContext)
        calloutContent.id = R.id.calloutTextView
        calloutContent.setTextColor(Color.BLACK)
        calloutContent.textSize = 18f

        val relativeParamsBelow = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        relativeParamsBelow.addRule(RelativeLayout.BELOW, calloutContent.id)

        // create attachment text view for the callout
        val calloutAttachment = TextView(applicationContext)
        calloutAttachment.id = R.id.attchTV
        calloutAttachment.setTextColor(Color.BLACK)
        calloutAttachment.textSize = 13f
        calloutContent.setPadding(0, 20, 20, 0)
        calloutAttachment.layoutParams = relativeParamsBelow

        val relativeParamsRightOf = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        relativeParamsRightOf.addRule(RelativeLayout.RIGHT_OF, calloutAttachment.id)

        // create image view for the callout
        val imageView = ImageView(applicationContext)
        imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.mipmap.ic_info))
        imageView.layoutParams = relativeParamsRightOf
        imageView.setOnClickListener(ImageViewOnclickListener())

        mCalloutLayout!!.addView(calloutContent)
        mCalloutLayout!!.addView(imageView)
        mCalloutLayout!!.addView(calloutAttachment)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            val noOfAttachments = data.extras.getInt(application.getString(R.string.noOfAttachments))
            // update the callout with attachment count
            showCallout(mSelectedArcGISFeatureAttributeValue!!, noOfAttachments)
        }

    }

    /**
     * Defines the listener for the ImageView clicks
     */
    private inner class ImageViewOnclickListener : View.OnClickListener {

        override fun onClick(v: View) {
            // start EditAttachmentActivity to view/edit the attachments
            val myIntent = Intent(this@EditFeatureAttachActivity, EditAttachmentActivity::class.java)
            myIntent.putExtra(application.getString(R.string.attribute), mAttributeID)
            myIntent.putExtra(application.getString(R.string.noOfAttachments), attachments!!.size)
            val bundle = Bundle()
            startActivityForResult(myIntent, REQUEST_CODE, bundle)

        }
    }

    companion object {

        private val TAG = "EditFeatureAttachment"
        private val REQUEST_CODE = 100
    }


}
