package com.shaunsheep.agsdemo.arcgistiledfromurl

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.activity_mapview.*


class TiledLayerFromUrl : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_arcgis_tiledlayer_url)

        // create new Tiled Layer from service url
        val tiledLayerBaseMap = ArcGISTiledLayer(resources.getString(R.string.world_topo_service))
        // set tiled layer as basemap
        val basemap = Basemap(tiledLayerBaseMap)
        // create a map with the basemap
        val map = ArcGISMap(basemap)
        // set the map to be displayed in this view
        mapView.map = map
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}
