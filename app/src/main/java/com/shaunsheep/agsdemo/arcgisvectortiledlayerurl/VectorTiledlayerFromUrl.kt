package com.shaunsheep.agsdemo.arcgisvectortiledlayerurl

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.arcgis_vectortiledlayer_url.*


class VectorTiledlayerFromUrl : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mVectorTiledLayer: ArcGISVectorTiledLayer? = null

    private var mNavigationDrawerItemTitles: Array<String>? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerList: ListView? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arcgis_vectortiledlayer_url)

        mMapView=mapView
        // create new Vector Tiled Layer from service url
        mVectorTiledLayer = ArcGISVectorTiledLayer(resources.getString(R.string.navigation_url))

        // set tiled layer as basemap
        val basemap = Basemap(mVectorTiledLayer)
        // create a map with the basemap
        val map = ArcGISMap(basemap)
        // create a viewpoint from lat, long, scale
        val vp = Viewpoint(47.606726, -122.335564, 72223.819286)
        // set initial map extent
        map.initialViewpoint = vp
        // set the map to be displayed in this view
        mMapView!!.map=map//不加两个!!会出现如下错误提示：
        // Smart cast to 'MapView!' is impossible, because 'mMapView' is a mutable property that could have been changed by this time

        // inflate navigation drawer
        mNavigationDrawerItemTitles = resources.getStringArray(R.array.vector_tiled_types)
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerList = findViewById(R.id.left_drawer) as ListView

        // Set the adapter for the list view
        mDrawerList!!.setAdapter(ArrayAdapter(this,
                R.layout.arcgis_vectorlayer_url_drawer_list_item, mNavigationDrawerItemTitles))
        // Set the list's click listener
        mDrawerList!!.setOnItemClickListener(DrawerItemClickListener())

        // set the navigation vector tiled layer item in the navigation drawer to selected
        mDrawerList!!.setItemChecked(0, true)

        setupDrawer()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        title = getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles!![0])
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()//不加两个!!会出如下错误提示：
        // Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type MapView?
    }

    /**
     * The click listener for ListView in the navigation drawer
     */
    private inner class DrawerItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            selectItem(position)
        }
    }

    private fun selectItem(position: Int) {

        // update selected item and title, then close the drawer
        mDrawerList!!.setItemChecked(position, true)//要加!!，否则提示如下：
        // Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type ListView?
        title = getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles!![position])
        mDrawerLayout!!.closeDrawer(mDrawerList)

        // update the MapView with the selected vector tiled layer type
        var vectorTiledLayerUrl: String? = null

        when (position) {
            0 -> vectorTiledLayerUrl = resources.getString(R.string.navigation_url)
            1 -> vectorTiledLayerUrl = resources.getString(R.string.night_url)
            2 -> vectorTiledLayerUrl = resources.getString(R.string.light_gray_url)
            3 -> vectorTiledLayerUrl = resources.getString(R.string.dark_gray_url)
        }
        // create the new vector tiled layer using the url
        mVectorTiledLayer = ArcGISVectorTiledLayer(vectorTiledLayerUrl!!)
        // change the basemap to the new layer
        mMapView!!.map.basemap = Basemap(mVectorTiledLayer)
    }

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

        mDrawerToggle!!.setDrawerIndicatorEnabled(true)
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.arcgis_vectortiledlayer_url, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

}
