package com.shaunsheep.agsdemo.maprotation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.SeekBar
import android.widget.TextView

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class MapRotationActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mRotationValueText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_rotation_activity)
        setTitle(R.string.title_map_rotation)

        // create MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the Basemap.Type topographic
        val mMap = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 10)
        // set the map to be displayed in this view
        mMapView!!.map = mMap
        // create TextView to show angle of rotation
        mRotationValueText = findViewById(R.id.rotationValueText) as TextView
        // create SeekBar
        val mRotationSeekBar = findViewById(R.id.rotationSeekBar) as SeekBar
        mRotationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, angle: Int, b: Boolean) {
                // convert progress to double
                val dAngle = angle.toDouble()
                // set the text to SeekBar value
                mRotationValueText!!.text = angle.toString()
                // rotate MapView to double angle value
                mMapView!!.setViewpointRotationAsync(dAngle)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
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
        if(keyCode== KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
