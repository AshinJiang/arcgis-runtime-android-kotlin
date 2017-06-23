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

package com.shaunsheep.agsdemo.openexistingmap

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.shaunsheep.agsdemo.R

class OpenExitingMapActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mMap: ArcGISMap? = null
    private var mPortal: Portal? = null
    private var mPortalItem: PortalItem? = null

    private var mDrawerList: ListView? = null

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mActivityTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.open_exiting_map_activity)
        setTitle(R.string.title_open_exiting_map)

        mDrawerList = findViewById(R.id.navList) as ListView
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mActivityTitle = title.toString()

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // get the portal url for ArcGIS Online
        mPortal = Portal(resources.getString(R.string.portal_url))
        // get the pre-defined portal id and portal url
        mPortalItem = PortalItem(mPortal!!, resources.getString(R.string.webmap_houses_with_mortgages_id))
        // create a map from a PortalItem
        mMap = ArcGISMap(mPortalItem!!)
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // add the webmap titles to the drawer
        addDrawerItems()
        setupDrawer()

        // set icons on action bar
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    private fun addDrawerItems() {
        val webmapTitles = arrayOf(resources.getString(R.string.webmap_houses_with_mortgages_title), resources.getString(R.string.webmap_usa_tapestry_segmentation_title), resources.getString(R.string.webmap_geology_us_title))
        val mAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, webmapTitles)
        mDrawerList!!.adapter = mAdapter

        mDrawerList!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            if (position == 0) {
                mPortalItem = PortalItem(mPortal!!, resources.getString(R.string.webmap_houses_with_mortgages_id))
                // create a map from a PortalItem
                mMap = ArcGISMap(mPortalItem!!)
                // set the map to be displayed in this view
                mMapView!!.map = mMap
                // close the drawer
                mDrawerLayout!!.closeDrawer(adapterView)
            } else if (position == 1) {
                mPortalItem = PortalItem(mPortal!!, resources.getString(R.string.webmap_usa_tapestry_segmentation_id))
                // create a map from a PortalItem
                mMap = ArcGISMap(mPortalItem!!)
                // set the map to be displayed in this view
                mMapView!!.map = mMap
                // close the drawer
                mDrawerLayout!!.closeDrawer(adapterView)
            } else if (position == 2) {
                mPortalItem = PortalItem(mPortal!!, resources.getString(R.string.webmap_geology_us))
                // create a map from a PortalItem
                mMap = ArcGISMap(mPortalItem!!)
                // set the map to be displayed in this view
                mMapView!!.map = mMap
                // close the drawer
                mDrawerLayout!!.closeDrawer(adapterView)
            }
        }
    }

    private fun setupDrawer() {
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            // called when drawer has settled in an open state
            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                // change the title to the nav bar
                supportActionBar!!.title = resources.getString(R.string.navbar_title)
                // invalidate options menu
                invalidateOptionsMenu()
            }

            // called when drawer has settled in a closed state
            override fun onDrawerClosed(view: View?) {
                super.onDrawerClosed(view)
                // set title to the app
                supportActionBar!!.title = mActivityTitle
                // invalidate options menu
                invalidateOptionsMenu()
            }
        }
        // enable draw indicator
        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        // attach toggle to drawer layout
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    }

    override fun onPause() {
        super.onPause()
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.resume()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // activate the navigation drawer toggle
        return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
