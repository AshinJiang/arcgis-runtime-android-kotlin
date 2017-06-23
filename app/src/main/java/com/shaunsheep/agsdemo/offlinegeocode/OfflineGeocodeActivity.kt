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

package com.shaunsheep.agsdemo.offlinegeocode

import java.util.HashMap
import java.util.concurrent.ExecutionException
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters
import com.shaunsheep.agsdemo.R

class OfflineGeocodeActivity : AppCompatActivity() {
    private val extern = Environment.getExternalStorageDirectory().path
    internal val requestCode = 2
    internal val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var graphicsOverlay: GraphicsOverlay? = null
    private var mGeocodeParameters: GeocodeParameters? = null
    private var mPinSourceSymbol: PictureMarkerSymbol? = null
    private var mMap: ArcGISMap?=null
    private var tiledLayer: ArcGISTiledLayer?=null
    private var mMapView: MapView? = null
    private var mLocatorTask: LocatorTask? = null
    private var mReverseGeocodeParameters: ReverseGeocodeParameters? = null
    private var mCallout: Callout? = null
    private var mSearchview: SearchView? = null
    private var mGraphicPointAddress: String? = null
    private var mGraphicPoint: Point? = null
    private var mGeocodedLocation: GeocodeResult? = null
    private var mSpinner: Spinner?=null
    private var isPinSelected: Boolean = false
    private var mCalloutContent: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.offline_geocode_activity)
        setTitle(R.string.title_offline_geocode)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // Check permissions to see if failure may be due to lack of permissions.
        val permissionCheck = ContextCompat.checkSelfPermission(this@OfflineGeocodeActivity, permission[0]) == PackageManager.PERMISSION_GRANTED

        if (!permissionCheck) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(this@OfflineGeocodeActivity, permission, requestCode)
        } else { // if permission was already granted, set up offline map and geocoding, reverse geocoding and LocatorTask
            setUpOfflineMapGeocoding()
            setSearchView()
        }
        mMapView!!.setOnTouchListener(MapTouchListener(applicationContext, mMapView!!))
    }

    private fun setSearchView() {
        mSearchview = findViewById(R.id.searchView1) as SearchView
        mSearchview!!.setIconifiedByDefault(true)
        mSearchview!!.queryHint = resources.getString(R.string.search_hint)
        mSearchview!!.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                hideKeyboard()
                geoCodeTypedAddress(query)
                mSearchview!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        mSpinner = findViewById(R.id.spinner) as Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = object : ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)
                if (position == count) {
                    mSearchview!!.clearFocus()
                }

                return v
            }

            override fun getCount(): Int {
                return super.getCount() - 1 // you dont display last item. It is used as hint.
            }

        }

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(*resources.getStringArray(R.array.suggestion_items))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // set vertical offset to spinner dropdown for API less than 21
            mSpinner!!.dropDownVerticalOffset = 80
        }
        // Apply the adapter to the spinner
        mSpinner!!.adapter = adapter
        mSpinner!!.setSelection(adapter.count)


        mSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == adapter.count) {
                    mSearchview!!.clearFocus()
                } else {
                    hideKeyboard()
                    mSearchview!!.setQuery(resources.getStringArray(R.array.suggestion_items)[position], false)
                    geoCodeTypedAddress(resources.getStringArray(R.array.suggestion_items)[position])
                    mSearchview!!.isIconified = false
                    mSearchview!!.clearFocus()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun setUpOfflineMapGeocoding() {
        // create a basemap from a local tile package
        val tileCache = TileCache(extern + resources.getString(R.string.sandiego_tpk))
        tiledLayer = ArcGISTiledLayer(tileCache)
        val basemap = Basemap(tiledLayer)

        // create ArcGISMap with imagery basemap
        mMap = ArcGISMap(basemap)

        mMapView!!.map = mMap

        mMap!!.addDoneLoadingListener {
            val p = Point(-117.162040, 32.718260, SpatialReference.create(4326))
            val vp = Viewpoint(p, 10000.0)
            mMapView!!.setViewpointAsync(vp, 3f)
        }

        // add a graphics overlay
        graphicsOverlay = GraphicsOverlay()
        graphicsOverlay!!.selectionColor = 0xFF00FFFF.toInt()
        mMapView!!.graphicsOverlays.add(graphicsOverlay)


        mGeocodeParameters = GeocodeParameters()
        mGeocodeParameters!!.resultAttributeNames.add("*")
        mGeocodeParameters!!.maxResults = 1

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        val startDrawable = ContextCompat.getDrawable(this, R.mipmap.pin) as BitmapDrawable
        mPinSourceSymbol = PictureMarkerSymbol(startDrawable)
        mPinSourceSymbol!!.height = 90f
        mPinSourceSymbol!!.width = 20f
        mPinSourceSymbol!!.loadAsync()
        mPinSourceSymbol!!.leaderOffsetY = 45f
        mPinSourceSymbol!!.offsetY = -48f

        mReverseGeocodeParameters = ReverseGeocodeParameters()
        mReverseGeocodeParameters!!.resultAttributeNames.add("*")
        mReverseGeocodeParameters!!.outputSpatialReference = mMap!!.spatialReference
        mReverseGeocodeParameters!!.maxResults = 1

        mLocatorTask = LocatorTask(extern + resources.getString(R.string.sandiego_loc))

        mCalloutContent = TextView(applicationContext)
        mCalloutContent!!.setTextColor(Color.BLACK)
        mCalloutContent!!.setTextIsSelectable(true)
    }

    /**
     * Geocode an address typed in by user

     * @param address
     */
    private fun geoCodeTypedAddress(address: String) {
        // Null out any previously located result
        mGeocodedLocation = null

        // Execute async task to find the address
        mLocatorTask!!.addDoneLoadingListener {
            if (mLocatorTask!!.loadStatus == LoadStatus.LOADED) {
                // Call geocodeAsync passing in an address
                val geocodeFuture = mLocatorTask!!.geocodeAsync(address,
                        mGeocodeParameters!!)
                geocodeFuture.addDoneListener(object : Runnable {
                    override fun run() {
                        try {
                            // Get the results of the async operation
                            val geocodeResults = geocodeFuture.get()

                            if (geocodeResults.size > 0) {
                                // Use the first result - for example
                                // display on the map
                                mGeocodedLocation = geocodeResults[0]
                                displaySearchResult(mGeocodedLocation!!.displayLocation, mGeocodedLocation!!.label)

                            } else {
                                Toast.makeText(applicationContext,
                                        getString(R.string.location_not_foud) + address,
                                        Toast.LENGTH_LONG).show()
                            }

                        } catch (e: InterruptedException) {
                            // Deal with exception...
                            e.printStackTrace()
                            Toast.makeText(applicationContext,
                                    getString(R.string.geo_locate_error),
                                    Toast.LENGTH_LONG).show()

                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                            Toast.makeText(applicationContext, getString(R.string.geo_locate_error), Toast.LENGTH_LONG).show()
                        }

                        // Done processing and can remove this listener.
                        geocodeFuture.removeDoneListener(this)
                    }
                })

            } else {
                Log.i(TAG, "Trying to reload locator task")
                mLocatorTask!!.retryLoadAsync()
            }
        }
        mLocatorTask!!.loadAsync()
    }

    /**
     * Hides soft keyboard
     */
    private fun hideKeyboard() {
        mSearchview!!.clearFocus()
        val inputManager = applicationContext
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(mSearchview!!.windowToken, 0)
    }

    private fun displaySearchResult(resultPoint: Point, address: String) {


        if (mMapView!!.callout.isShowing) {
            mMapView!!.callout.dismiss()
        }
        //remove any previous graphics/search results
        //mMapView.getGraphicsOverlays().clear();
        graphicsOverlay!!.graphics.clear()
        // create graphic object for resulting location
        val resultLocGraphic = Graphic(resultPoint, mPinSourceSymbol!!)
        // add graphic to location layer
        graphicsOverlay!!.graphics.add(resultLocGraphic)

        // Zoom map to geocode result location
        mMapView!!.setViewpointAsync(Viewpoint(resultPoint, 8000.0), 3f)

        mGraphicPoint = resultPoint
        mGraphicPointAddress = address
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            setUpOfflineMapGeocoding()
            setSearchView()
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(this@OfflineGeocodeActivity, resources.getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show()

        }
    }

    private inner class DragTouchListener(context: Context, mapView: MapView) : DefaultMapViewOnTouchListener(context, mapView) {

        internal var dX: Float = 0.toFloat()
        internal var dY: Float = 0.toFloat()

        override fun onTouch(view: View?, event: MotionEvent): Boolean {

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    dX = view!!.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val pointerIndex = MotionEventCompat.getActionIndex(event)
                    val x = MotionEventCompat.getX(event, pointerIndex)
                    val y = MotionEventCompat.getY(event, pointerIndex)
                    val screenPoint = android.graphics.Point(Math.round(x), Math.round(y))
                    val singleTapPoint = mMapView.screenToLocation(screenPoint)
                    val results = mLocatorTask!!.reverseGeocodeAsync(singleTapPoint,
                            mReverseGeocodeParameters!!)
                    graphicsOverlay!!.graphics.clear()
                    val resultLocGraphic = Graphic(singleTapPoint, mPinSourceSymbol!!)
                    resultLocGraphic.isSelected = true
                    // add graphic to location layer
                    graphicsOverlay!!.graphics.add(resultLocGraphic)
                    // display callout with reverse-geocode result on UI thread
                    runOnUiThread {
                        try {
                            val geocodes = results.get()
                            if (geocodes.size > 0) {
                                // get the top result
                                val geocode = geocodes[0]
                                val detail: String
                                // attributes from a click-based search
                                val street = geocode.attributes["Street"].toString()
                                val city = geocode.attributes["City"].toString()
                                val state = geocode.attributes["State"].toString()
                                val zip = geocode.attributes["ZIP"].toString()
                                detail = "$city, $state $zip"

                                val address = street + "," + detail
                                mCalloutContent!!.text = address
                                // get callout, set content and show
                                mCallout = mMapView.callout
                                mCallout!!.location = singleTapPoint
                                mCallout!!.content = mCalloutContent!!
                                mCallout!!.show()

                                mGraphicPoint = singleTapPoint
                                mGraphicPointAddress = address
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> if (graphicsOverlay!!.graphics.size > 0) {
                    graphicsOverlay!!.graphics[0].isSelected = false
                    isPinSelected = false
                    mMapView.setOnTouchListener(MapTouchListener(applicationContext, mMapView))
                }
                else -> return false
            }
            return true
        }
    }

    private inner class MapTouchListener(context: Context, mapView: MapView) : DefaultMapViewOnTouchListener(context, mapView) {

        override fun onLongPress(e: MotionEvent) {
            val screenPoint = android.graphics.Point(Math.round(e.x),
                    Math.round(e.y))

            val longPressPoint = mMapView.screenToLocation(screenPoint)

            val results = mLocatorTask!!.reverseGeocodeAsync(longPressPoint,
                    mReverseGeocodeParameters!!)
            results.addDoneListener(ResultsLoadedListener(results))

        }


        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {


            if (mMapView.callout.isShowing) {
                mMapView.callout.dismiss()
            }
            if (graphicsOverlay!!.graphics.size > 0) {
                if (graphicsOverlay!!.graphics[0].isSelected) {
                    isPinSelected = false
                    graphicsOverlay!!.graphics[0].isSelected = false
                }
            }
            // get the screen point where user tapped
            val screenPoint = android.graphics.Point(e!!.x.toInt(), e.y.toInt())

            // identify graphics on the graphics overlay
            val identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 1.0, false, 1)

            identifyGraphic.addDoneListener {
                try {
                    val grOverlayResult = identifyGraphic.get()
                    // get the list of graphics returned by identify
                    val graphic = grOverlayResult.graphics
                    // if identified graphic is not empty, start DragTouchListener
                    if (!graphic.isEmpty()) {

                        if (!isPinSelected) {
                            isPinSelected = true
                            graphic[0].isSelected = true
                            Toast.makeText(applicationContext,
                                    getString(R.string.reverse_geocode_message),
                                    Toast.LENGTH_SHORT).show()
                            mMapView.setOnTouchListener(DragTouchListener(applicationContext, mMapView))
                        }

                        mCalloutContent!!.text = mGraphicPointAddress
                        // get callout, set content and show
                        mCallout = mMapView.callout
                        mCallout!!.content = mCalloutContent!!
                        mCallout!!.location = mGraphicPoint!!
                        mCallout!!.show()
                    }
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                } catch (ie: ExecutionException) {
                    ie.printStackTrace()
                }
            }

            return super.onSingleTapConfirmed(e)
        }
    }

    /**
     * Updates marker and callout when new results are loaded.
     */
    private inner class ResultsLoadedListener
    /**
     * Constructs a runnable listener for the geocode results.

     * @param results results from a [LocatorTask.geocodeAsync] task
     */
    internal constructor(private val results: ListenableFuture<List<GeocodeResult>>) : Runnable {


        override fun run() {

            try {
                val geocodes = results.get()
                if (geocodes.size > 0) {
                    // get the top result
                    val geocode = geocodes[0]

                    // set the viewpoint to the marker
                    val location = geocode.displayLocation
                    // get attributes from the result for the callout
                    val title: String
                    val detail: String
                    val matchAddr = geocode.attributes["Match_addr"]
                    if (matchAddr != null) {
                        // attributes from a query-based search
                        title = matchAddr.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                        detail = matchAddr.toString().substring(matchAddr.toString().indexOf(",") + 1)
                    } else {
                        // attributes from a click-based search
                        val street = geocode.attributes["Street"].toString()
                        val city = geocode.attributes["City"].toString()
                        val state = geocode.attributes["State"].toString()
                        val zip = geocode.attributes["ZIP"].toString()
                        title = street
                        detail = "$city, $state $zip"
                    }

                    // get attributes from the result for the callout
                    val attributes = HashMap<String, Any>()
                    attributes.put("title", title)
                    attributes.put("detail", detail)


                    // create the marker
                    val marker = Graphic(geocode.displayLocation, attributes, mPinSourceSymbol!!)
                    graphicsOverlay!!.graphics.clear()

                    // add the markers to the graphics overlay
                    graphicsOverlay!!.graphics.add(marker)

                    if (isPinSelected) {
                        marker.isSelected = true
                    }
                    val calloutText = title + ", " + detail
                    mCalloutContent!!.text = calloutText
                    // get callout, set content and show
                    mCallout = mMapView!!.callout
                    mCallout!!.location = geocode.displayLocation
                    mCallout!!.content = mCalloutContent!!
                    mCallout!!.show()

                    mGraphicPoint = location
                    mGraphicPointAddress = title + ", " + detail
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    companion object {
        private val TAG = "OfflineActivity"
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
