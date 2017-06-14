package com.shaunsheep.agsdemo.changefeaturelayerrenderer

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import com.shaunsheep.agsdemo.R
import com.esri.arcgisruntime.layers.FeatureLayer
import kotlinx.android.synthetic.main.change_featurelayer_renderer_activity.*
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.ArcGISMap

class ChangeFeatureLayerRendererActivity : AppCompatActivity() {

    var mFeatureLayer: FeatureLayer? = null
    var overrideActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_featurelayer_renderer_activity)
        setTitle(R.string.change_featurelayer_renderer)

        // set up the bottom toolbar
        createBottomToolbar()

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())
        //set an initial viewpoint
        map.initialViewpoint = Viewpoint(Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7, 4016869.78617381, SpatialReferences.getWebMercator()))

        // create feature layer with its service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.change_featurelayer_sample_service_url))
        mFeatureLayer = FeatureLayer(serviceFeatureTable)

        // add the layer to the map
        map.operationalLayers.add(mFeatureLayer)

        // set the map to be displayed in the mapview
        mapView.map = map
    }

    private fun createBottomToolbar() {

        val bottomToolbar = findViewById(R.id.bottomToolbar) as Toolbar
        bottomToolbar.inflateMenu(R.menu.change_featurelayer_renderer)

//        bottomToolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
//            override fun onMenuItemClick(item: MenuItem): Boolean {
        bottomToolbar.setOnMenuItemClickListener { item ->
            // Handle action bar item clicks
            val itemId = item.itemId
            //if statement is used because this sample is used elsewhere as a Library module
            if (itemId == R.id.action_override_rend) {
                // check the state of the menu item
                if (!overrideActive) {
                    overrideRenderer()
                    // change the text to reset
                    overrideActive = true
                    item.setTitle(R.string.change_featurelayer_action_reset)
                } else {
                    resetRenderer()
                    // change the text to override
                    overrideActive = false
                    item.setTitle(R.string.change_featurelayer_action_override_rend)
                }
            }
            true
        }
    }

    private fun overrideRenderer() {
        // create a new simple renderer for the line feature layer
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 255), 2f)
        val simpleRenderer = SimpleRenderer(lineSymbol)

        // override the current renderer with the new renderer defined above
        mFeatureLayer!!.renderer = simpleRenderer
    }

    private fun resetRenderer() {
        // reset the renderer back to the definition from the source (feature service) using the reset renderer method
        mFeatureLayer!!.resetRenderer()
    }

    override fun onPause() {
        super.onPause()
        // pause MapView
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        // resume MapView
        mapView.resume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
