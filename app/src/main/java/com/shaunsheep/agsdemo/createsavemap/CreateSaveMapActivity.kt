package com.shaunsheep.agsdemo.createsavemap

import android.app.ProgressDialog
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.security.OAuthLoginManager
import com.shaunsheep.agsdemo.R


class CreateSaveMapActivity : AppCompatActivity() {
    private val MIN_SCALE = 40000000
    private val SCALE = 50000000
    companion object {
        var mMap: ArcGISMap? = null
        var oauthLoginManager: OAuthLoginManager? = null

        fun getOAuthLoginManagerInstance(): OAuthLoginManager {
            return oauthLoginManager!!
        }
    }
    private var progressDialog: ProgressDialog? = null
    private var viewpoint: Viewpoint? = null
    private var mMapView: MapView? = null
    private var mBasemapTiles: Array<String>? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mBasemapListView: ListView? = null
    private var mLayerListView: ListView? = null
    private var mDrawerTitle: CharSequence? = null
    private var mTitle: CharSequence? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private val layer_array = arrayOfNulls<Layer>(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_save_map_drawer)
        title=(resources.getString(R.string.title_create_save_map))

        mDrawerTitle = title
        mTitle = mDrawerTitle
        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with Topographic Basemap
        mMap = ArcGISMap(Basemap.Type.STREETS, 48.354388, -99.998245, 3)
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // create spatial reference for all points
        val spatialReference = SpatialReferences.getWebMercator()

        // create the esri location point
        val londonPoint = Point(-14093.0, 6711377.0, spatialReference)
        // create the viewpoint with the London point and scale
        viewpoint = Viewpoint(londonPoint, SCALE.toDouble())

        val tiledLayer = ArcGISTiledLayer(application.getString(R.string.world_elevation))
        val imageLayer = ArcGISMapImageLayer(application.getString(R.string.world_census))
        // setting the scales at which this layer can be viewed
        imageLayer.minScale = MIN_SCALE.toDouble()
        imageLayer.maxScale = MIN_SCALE.toDouble() / 10

        layer_array[0] = tiledLayer
        layer_array[1] = imageLayer

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle(application.getString(R.string.author_map_message))
        progressDialog!!.setMessage(application.getString(R.string.wait))

        // create arrays from String arrays
        mBasemapTiles = resources.getStringArray(R.array.basemap_array)
        val mLayerTiles = resources.getStringArray(R.array.operational_layer_array)

        // inflate the Basemap and Layer list views
        mBasemapListView = findViewById(R.id.basemap_list) as ListView
        mLayerListView = findViewById(R.id.layer_list) as ListView

        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout

        // Set the adapter for the Basemap list view
        mBasemapListView!!.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, mBasemapTiles)
        mBasemapListView!!.setItemChecked(0, true)

        // Set the adapter for the Operational Layer list view
        mLayerListView!!.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, mLayerTiles)
        mBasemapListView!!.onItemClickListener = BasemapClickListener()

        // set actions for drawer state - close/open
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.title_create_save_map, R.string.title_create_save_map) {
            // if the drawer is closed, get the checked items from LayerListView and add the checked layer
            override fun onDrawerClosed(view: View) {
                supportActionBar!!.title = mTitle
                mMap!!.addDoneLoadingListener(Runnable {
                    if (mMap!!.loadStatus.name.equals(LoadStatus.LOADED.name)) {
                        // if both items are checked, add them
                        if (mLayerListView!!.checkedItemCount > 1) {
                            progressDialog!!.show()
                            removeLayers()
                            mMap!!.operationalLayers.add(layer_array[0])
                            mMap!!.operationalLayers.add(layer_array[1])
                        } else { // if any one item is checked, add as layer
                            if (mLayerListView!!.isItemChecked(0)) {
                                progressDialog!!.show()
                                removeLayers()
                                mMap!!.operationalLayers.add(layer_array[0])
                            } else if (mLayerListView!!.isItemChecked(1)) {
                                progressDialog!!.show()
                                removeLayers()
                                mMap!!.operationalLayers.add(layer_array[1])
                            } else {
                                removeLayers()
                            }
                        }
                    }
                })
                // if the progress dialog is showing, dismiss it
                mMapView!!.addLayerViewStateChangedListener(LayerViewStateChangedListener {
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                })
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                supportActionBar!!.title=(mDrawerTitle)
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu()

            }
        }

        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_save_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // toggle nav drawer on selecting action bar app icon/title
        val id = item.itemId


        if (id == R.id.action_save) {
            oAuthBrowser()
        }

        // Activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    /**
     * launch the OAuth browser page to get credentials
     */
    private fun oAuthBrowser() {
        try {
            // create a OAuthLoginManager object with portalURL, clientID, redirectUri and expiration
            val portalSettings = resources.getStringArray(R.array.portal)
            oauthLoginManager = OAuthLoginManager(portalSettings[1], portalSettings[2], portalSettings[3], 0)
            // launch the browser to get the credentials
            oauthLoginManager!!.launchOAuthBrowserPage(applicationContext)

        } catch (e: Exception) {
            Log.e("error-", e.message + "")
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // if nav drawer is opened, hide the action items
        menu.findItem(R.id.action_save).isVisible = mDrawerLayout!!.isDrawerOpen(GravityCompat.START)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun setTitle(title: CharSequence) {
        mTitle = title
        super.setTitle(title)
    }

    /**
     * sync the state of the drawer toggle
     */

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggle
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    /**
     * create a ArcGISMap for the selected position

     * @param choice chosen Basemap
     */
    private fun setBasemap(choice: Int) {
        removeLayers()
        when (choice) {
            0 -> mMap!!.basemap = Basemap.createStreets()
            1 -> mMap!!.basemap = Basemap.createImagery()
            2 -> mMap!!.basemap = Basemap.createTopographic()
            3 -> mMap!!.basemap = Basemap.createOceans()
        }
    }

    /**
     * Remove the operational layers from the Map
     */
    private fun removeLayers() {
        if (mMap!!.operationalLayers.size === 2) {
            for (i in 0..mMap!!.operationalLayers.size - 1) {
                mMap!!.operationalLayers.removeAt(0)
            }
        }
        if (mMap!!.operationalLayers.size === 1) {
            mMap!!.operationalLayers.removeAt(0)
        }
    }

    /**
     * Class BasemapClickListener listens for item click and sets the Basemap for selected choice.
     */
    private inner class BasemapClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            mBasemapListView!!.setSelection(position)
            // set the basemap
            setBasemap(position)
            Toast.makeText(this@CreateSaveMapActivity, "Selected " + mBasemapTiles!![position], Toast.LENGTH_SHORT).show()
        }
    }
}
