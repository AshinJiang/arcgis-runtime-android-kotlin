package com.shaunsheep.agsdemo.changesublayervisible

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.activity_mapview.*
import com.esri.arcgisruntime.layers.SublayerList
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.ArcGISMap

class ChangeSublayerVisibleActivity : AppCompatActivity() {
    private var mMapImageLayer: ArcGISMapImageLayer? = null
    private var mLayers: SublayerList? = null

    // The layer on/off menu items.
    private var mCities: MenuItem? = null
    private var mContinent: MenuItem? = null
    private var mWorld: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.change_sublayer_visible)

        // create a map with the Basemap Type topographic
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 48.354406, -99.998267, 2)
        // create a MapImageLayer with dynamically generated map images
        mMapImageLayer = ArcGISMapImageLayer(resources.getString(R.string.world_cities_service))
        mMapImageLayer!!.opacity=0.5f
        // add world cities layers as map operational layer
        map.operationalLayers.add(mMapImageLayer)
        // set the map to be displayed in this view
        mapView.map=map
        // get the layers from the map image layer
        mLayers = mMapImageLayer!!.sublayers
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.change_sublayer_visible, menu)

        // Get the sub layer switching menu items.
        mCities = menu.getItem(0)
        mContinent = menu.getItem(1)
        mWorld = menu.getItem(2)

        // set all layers on by default
        mCities!!.isChecked=true
        mContinent!!.isChecked=true
        mWorld!!.isChecked=true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle menu item selection
        //if-else is used because this sample is used elsewhere as a Library module
        val itemId = item.itemId
        if (itemId == R.id.Cities) {
            if (mLayers!![0].isVisible && mCities!!.isChecked) {
                // cities layer is on and menu item checked
                mLayers!![0].isVisible = false
                mCities!!.isChecked=false
            } else if (!mLayers!![0].isVisible && !mCities!!.isChecked) {
                // cities layer is off and menu item unchecked
                mLayers!![0].isVisible = true
                mCities!!.isChecked=true
            }
            return true
        } else if (itemId == R.id.Continents) {
            if (mLayers!![1].isVisible && mContinent!!.isChecked) {
                // continent layer is on and menu item checked
                mLayers!![1].isVisible = false
                mContinent!!.isChecked=false
            } else if (!mLayers!![1].isVisible && !mContinent!!.isChecked) {
                // continent layer is off and menu item unchecked
                mLayers!![1].isVisible = true
                mContinent!!.isChecked=true
            }
            return true
        } else if (itemId == R.id.World) {
            if (mLayers!![2].isVisible && mWorld!!.isChecked) {
                // world layer is on and menu item checked
                mLayers!![2].isVisible = false
                mWorld!!.isChecked=false
            } else if (!mLayers!![2].isVisible && !mWorld!!.isChecked) {
                // world layer is off and menu item unchecked
                mLayers!![2].isVisible = true
                mWorld!!.isChecked=true
            }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode==KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
