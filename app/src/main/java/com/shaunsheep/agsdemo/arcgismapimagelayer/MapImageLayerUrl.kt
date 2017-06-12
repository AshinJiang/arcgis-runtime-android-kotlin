package com.shaunsheep.agsdemo.arcgismapimagelayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.activity_mapview.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer

class MapImageLayerUrl : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.arcgis_mapimagelayer_url)

        // create a MapImageLayer with dynamically generated map images
        val mapImageLayer = ArcGISMapImageLayer(resources.getString(R.string.world_elevation_service))
        // create an empty map instance
        val map = ArcGISMap()
        // add map image layer as operational layer
        map.operationalLayers.add(mapImageLayer)
        // set the map to be displayed in this view
        mapView.map=map
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
        if (keyCode== KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}
