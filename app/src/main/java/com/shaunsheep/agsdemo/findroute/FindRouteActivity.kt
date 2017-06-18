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

package com.shaunsheep.agsdemo.findroute

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.shaunsheep.agsdemo.R

class FindRouteActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null

    private var mMapView: MapView? = null
    private var mRouteTask: RouteTask? = null
    private var mRouteParams: RouteParameters? = null
    private var mSourcePoint: Point? = null
    private var mDestinationPoint: Point? = null
    private var mRoute: Route? = null
    private var mRouteSymbol: SimpleLineSymbol? = null
    private var mGraphicsOverlay: GraphicsOverlay? = null

    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerList: ListView? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_route_drawer_activity)
        setTitle(R.string.title_find_route)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create new Vector Tiled Layer from service url
        val mVectorTiledLayer = ArcGISVectorTiledLayer(
                resources.getString(R.string.navigation_vector))

        // set tiled layer as basemap
        val basemap = Basemap(mVectorTiledLayer)
        // create a map with the basemap
        val mMap = ArcGISMap(basemap)
        // create a viewpoint from lat, long, scale
        val sanDiegoPoint = Viewpoint(32.7157, -117.1611, 200000.0)
        // set initial map extent
        mMap.initialViewpoint = sanDiegoPoint
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // inflate navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerList = findViewById(R.id.left_drawer) as ListView

        val mDirectionFab = findViewById(R.id.fab) as FloatingActionButton

        setupDrawer()

        setupSymbols()

        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setTitle(getString(R.string.find_route_progress_title))
        mProgressDialog!!.setMessage(getString(R.string.progress_message))


        mDirectionFab.setOnClickListener {
            mProgressDialog!!.show()

            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeButtonEnabled(true)
                title = getString(R.string.title_find_route)
            }
            mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

            // create RouteTask instance
            mRouteTask = RouteTask(getString(R.string.routing_service))

            val listenableFuture = mRouteTask!!.createDefaultParametersAsync()
            listenableFuture.addDoneListener {
                try {
                    if (listenableFuture.isDone) {
                        var i = 0
                        mRouteParams = listenableFuture.get()

                        // get List of Stops
                        val routeStops = mRouteParams!!.stops
                        // set return directions as true to return turn-by-turn directions in the result of getDirectionManeuvers().
                        mRouteParams!!.isReturnDirections = true

                        // add your stops to it 32.7254716,-117.1508181 32.7076359,-117.1592837 -117.15557279683529
                        //-13041171, 3860988, SpatialReference(3857) -13041693, 3856006, SpatialReference(3857)
                        routeStops.add(Stop(Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())))
                        routeStops.add(Stop(Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())))

                        // solve
                        val result = mRouteTask!!.solveRouteAsync(mRouteParams!!).get()
                        val routes = result.routes
                        mRoute = routes[0] as Route
                        // create a mRouteSymbol graphic
                        val routeGraphic = Graphic(mRoute!!.routeGeometry, mRouteSymbol!!)
                        // add mRouteSymbol graphic to the map
                        mGraphicsOverlay!!.graphics.add(routeGraphic)
                        // get directions
                        // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
                        val directions = mRoute!!.directionManeuvers

                        val directionsArray = arrayOfNulls<String>(directions.size)

                        for (dm in directions) {
                            directionsArray[i++] = dm.directionText
                        }
                        Log.d(TAG, directions[0].geometry.extent.xMin.toString() + "")
                        Log.d(TAG, directions[0].geometry.extent.yMin.toString() + "")

                        // Set the adapter for the list view
                        mDrawerList!!.adapter = ArrayAdapter<String>(applicationContext,
                                R.layout.find_route_drawer_layout_text, directionsArray)

                        if (mProgressDialog!!.isShowing) {
                            mProgressDialog!!.dismiss()
                        }
                        mDrawerList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            if (mGraphicsOverlay!!.graphics.size > 3) {
                                mGraphicsOverlay!!.graphics.removeAt(mGraphicsOverlay!!.graphics.size - 1)
                            }
                            mDrawerLayout!!.closeDrawers()
                            val dm = directions[position]
                            val gm = dm.geometry
                            val vp = Viewpoint(gm.extent, 20.0)
                            mMapView!!.setViewpointAsync(vp, 3f)
                            val selectedRouteSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 5f)
                            val selectedRouteGraphic = Graphic(directions[position].geometry, selectedRouteSymbol)
                            mGraphicsOverlay!!.graphics.add(selectedRouteGraphic)
                        }

                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message)
                }
            }
        }


    }

    /**
     * Set up the Source, Destination and mRouteSymbol graphics symbol
     */
    private fun setupSymbols() {

        mGraphicsOverlay = GraphicsOverlay()

        //add the overlay to the map view
        mMapView!!.graphicsOverlays.add(mGraphicsOverlay)

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        val startDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_source) as BitmapDrawable
        val pinSourceSymbol = PictureMarkerSymbol(startDrawable)
        pinSourceSymbol.loadAsync()
        //[DocRef: END]
        pinSourceSymbol.addDoneLoadingListener {
            //add a new graphic as start point
            mSourcePoint = Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())
            val pinSourceGraphic = Graphic(mSourcePoint!!, pinSourceSymbol)
            mGraphicsOverlay!!.graphics.add(pinSourceGraphic)
        }
        pinSourceSymbol.offsetY = 20f
        val endDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_destination) as BitmapDrawable
        val pinDestinationSymbol = PictureMarkerSymbol(endDrawable)
        pinDestinationSymbol.loadAsync()
        //[DocRef: END]
        pinDestinationSymbol.addDoneLoadingListener {
            //add a new graphic as end point
            mDestinationPoint = Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())
            val destinationGraphic = Graphic(mDestinationPoint!!, pinDestinationSymbol)
            mGraphicsOverlay!!.graphics.add(destinationGraphic)
        }
        pinDestinationSymbol.offsetY = 20f

        mRouteSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5f)
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

    /**
     * set up the drawer
     */
    private fun setupDrawer() {
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View?) {
                super.onDrawerClosed(view)
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }

        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)

        mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onConfigurationChanged(newConfig)

        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    companion object {

        private val TAG = "FindRouteSample"
    }
}
