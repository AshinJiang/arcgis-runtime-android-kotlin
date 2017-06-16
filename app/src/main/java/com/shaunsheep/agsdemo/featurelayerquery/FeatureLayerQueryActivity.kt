/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.shaunsheep.agsdemo.featurelayerquery

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.widget.Toast

import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.shaunsheep.agsdemo.R

class FeatureLayerQueryActivity : AppCompatActivity() {

    private var mMapView: MapView?=null
    private var mServiceFeatureTable: ServiceFeatureTable?=null
    private var mFeaturelayer: FeatureLayer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_feature_layer_query)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())
        // set the map to be displayed in the mapview
        mMapView!!.map = map

        // create feature layer with its service feature table
        // create the service feature table
        mServiceFeatureTable = ServiceFeatureTable(resources.getString(R.string.feature_layer_query_service_url))
        // create the feature layer using the service feature table
        mFeaturelayer = FeatureLayer(mServiceFeatureTable)
        mFeaturelayer!!.opacity = 0.8f
        //override the renderer
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1f)
        val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, lineSymbol)
        mFeaturelayer!!.renderer = SimpleRenderer(fillSymbol)

        // add the layer to the map
        map.operationalLayers.add(mFeaturelayer)

        // zoom to a view point of the USA
        mMapView!!.setViewpointCenterAsync(Point(-11000000.0, 5000000.0, SpatialReferences.getWebMercator()), 100000000.0)
    }

    /**
     * Handle the search intent from the search widget
     */
    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val searchString = intent.getStringExtra(SearchManager.QUERY)

            searchForState(searchString)
        }
    }

    fun searchForState(searchString: String) {

        // clear any previous selections
        mFeaturelayer!!.clearSelection()

        // create objects required to do a selection with a query
        val query = QueryParameters()
        //make search case insensitive
        query.whereClause = "upper(STATE_NAME) LIKE '%" + searchString.toUpperCase() + "%'"

        // call select features
        val future = mServiceFeatureTable!!.queryFeaturesAsync(query)
        // add done loading listener to fire when the selection returns
        future.addDoneListener {
            try {
                // call get on the future to get the result
                val result = future.get()

                // check there are some results
                if (result.iterator().hasNext()) {

                    // get the extend of the first feature in the result to zoom to
                    val feature = result.iterator().next()
                    val envelope = feature.geometry.extent
                    mMapView!!.setViewpointGeometryAsync(envelope, 20.0)

                    //Select the feature
                    mFeaturelayer!!.selectFeature(feature)

                } else {
                    Toast.makeText(this@FeatureLayerQueryActivity, "No states found with name: " + searchString, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FeatureLayerQueryActivity, "Feature search failed for: " + searchString + ". Error=" + e.message, Toast.LENGTH_SHORT).show()
                Log.e(resources.getString(R.string.app_name), "Feature search failed for: " + searchString + ". Error=" + e.message)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.feature_layer_query, menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default

        return true
    }

    override fun onPause() {
        super.onPause()
        // pause MapView
        mMapView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        // resume MapView
        mMapView!!.resume()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
