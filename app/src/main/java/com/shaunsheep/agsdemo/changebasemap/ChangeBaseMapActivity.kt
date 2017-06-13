package com.shaunsheep.agsdemo.changebasemap

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.shaunsheep.agsdemo.R
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.Basemap
import android.widget.ArrayAdapter

class ChangeBaseMapActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mMap: ArcGISMap? = null

    private var mNavigationDrawerItemTitles: Array<String>? = null

    private var mDrawerList: ListView? = null

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mActivityTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_basemap_activity)
        setTitle(R.string.change_basemap)

        // inflate navigation drawer
        mNavigationDrawerItemTitles = resources.getStringArray(R.array.vector_tiled_types)
        mDrawerList = findViewById(R.id.navList) as ListView
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        // get app title
        mActivityTitle = title.toString()

        addDrawerItems()
        setupDrawer()

        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            // set opening basemap title to Topographic
            supportActionBar!!.title=mNavigationDrawerItemTitles!![2]
        }

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with Topographic Basemap
        mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.6047381, -122.3334255, 12)
        // set the map to be displayed in this view
        mMapView!!.map=mMap
    }

    /**
     * Add navigation drawer items
     */
    private fun addDrawerItems() {
        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mNavigationDrawerItemTitles)
        mDrawerList!!.adapter=mAdapter

//        mDrawerList!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
//            override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
//                selectBasemap(position)
//            }
//        })
        //上面等价如下 kotlin 写法：更简洁
        mDrawerList!!.setOnItemClickListener{adapterView,view,position,id ->
            selectBasemap(position)
        }
    }

    /**
     * Set up the navigation drawer
     */
    private fun setupDrawer() {

        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                supportActionBar!!.title=mActivityTitle
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View?) {
                super.onDrawerClosed(view)
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }

        mDrawerToggle!!.isDrawerIndicatorEnabled=true
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)
    }

    /**
     * Select the Basemap item based on position in the navigation drawer

     * @param position order int in navigation drawer
     */
    private fun selectBasemap(position: Int) {
        // update selected item and title, then close the drawer
        mDrawerList!!.setItemChecked(position, true)
        mDrawerLayout!!.closeDrawer(mDrawerList)

        // if-else is used because this sample is used elsewhere as a Library module
        if (position == 0) {
            // position 0 = Streets
            mMap!!.basemap =Basemap.createStreets()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        } else if (position == 1) {
            // position 1 = Navigation Vector
            mMap!!.basemap =Basemap.createNavigationVector()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        } else if (position == 2) {
            // position 2 = Topographic
            mMap!!.basemap =Basemap.createTopographic()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        } else if (position == 3) {
            // position 3 = Topographic Vector
            mMap!!.basemap =Basemap.createTopographicVector()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        } else if (position == 4) {
            // position 3 = Gray Canvas
            mMap!!.basemap = Basemap.createLightGrayCanvas()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        } else if (position == 5) {
            // position 3 = Gray Canvas Vector
            mMap!!.basemap =Basemap.createLightGrayCanvasVector()
            supportActionBar!!.title=mNavigationDrawerItemTitles!![position]
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.pause()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode==KeyEvent.ACTION_DOWN) finish()
        return super.onKeyDown(keyCode, event)
    }
}
