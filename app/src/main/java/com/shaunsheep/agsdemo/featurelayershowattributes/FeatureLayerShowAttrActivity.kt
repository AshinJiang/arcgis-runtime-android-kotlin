package com.shaunsheep.agsdemo.featurelayershowattributes

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R
import java.text.SimpleDateFormat
import java.util.*

class FeatureLayerShowAttrActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mCallout: Callout? = null

    private var mServiceFeatureTable: ServiceFeatureTable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_feature_layer_show_attr)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create an ArcGISMap with BasemapType topo
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.057386, -117.191455, 14)
        // set the ArcGISMap to the MapView
        mMapView!!.map = map
        // get the callout that shows attributes
        mCallout = mMapView!!.callout
        // create the service feature table
        mServiceFeatureTable = ServiceFeatureTable(resources.getString(R.string.world_facilities_service_URL))
        // create the feature layer using the service feature table
        val featureLayer = FeatureLayer(mServiceFeatureTable!!)
        // add the layer to the map
        map.operationalLayers.add(featureLayer)

        // set an on touch listener to listen for click events
        mMapView!!.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                // remove any existing callouts
                if (mCallout!!.isShowing) {
                    mCallout!!.dismiss()
                }
                // get the point that was clicked and convert it to a point in map coordinates
                val clickPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e!!.x), Math.round(e.y)))
                // create a selection tolerance
                val tolerance = 10
                val mapTolerance = tolerance * mMapView.unitsPerDensityIndependentPixel
                // use tolerance to create an envelope to query
                val envelope = Envelope(clickPoint.x - mapTolerance, clickPoint.y - mapTolerance, clickPoint.x + mapTolerance, clickPoint.y + mapTolerance, map.spatialReference)
                val query = QueryParameters()
                query.geometry = envelope
                // request all available attribute fields
                val future = mServiceFeatureTable!!.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                // add done loading listener to fire when the selection returns
                future.addDoneListener {
                    try {
                        //call get on the future to get the result
                        val result = future.get()
                        // create an Iterator
                        val iterator = result.iterator()
                        // create a TextView to display field values
                        val calloutContent = TextView(applicationContext)
                        calloutContent.setTextColor(Color.BLACK)
                        calloutContent.setSingleLine(false)
                        calloutContent.isVerticalScrollBarEnabled = true
                        calloutContent.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
                        calloutContent.movementMethod = ScrollingMovementMethod()
                        calloutContent.setLines(5)
                        // cycle through selections
                        var counter = 0
                        var feature: Feature
                        while (iterator.hasNext()) {
                            feature = iterator.next()
                            // create a Map of all available attributes as name value pairs
                            val attr = feature.attributes
                            val keys = attr.keys
                            for (key in keys) {
                                var value = attr[key]
                                // format observed field value as date
                                if (value is GregorianCalendar) {
                                    val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                                    value = simpleDateFormat.format(value.time)
                                }
                                // append name value pairs to TextView
                                calloutContent.append(key + " | " + value + "\n")
                            }
                            counter++
                            // center the mapview on selected feature
                            val envelope = feature.geometry.extent
                            mMapView.setViewpointGeometryAsync(envelope, 200.0)
                            // show CallOut
                            mCallout!!.location = clickPoint
                            mCallout!!.content = calloutContent
                            mCallout!!.show()
                        }
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
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
