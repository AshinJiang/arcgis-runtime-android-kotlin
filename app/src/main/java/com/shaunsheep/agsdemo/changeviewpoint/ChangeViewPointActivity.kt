package com.shaunsheep.agsdemo.changeviewpoint

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import com.esri.arcgisruntime.geometry.Point
import com.shaunsheep.agsdemo.R
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import android.support.v4.content.ContextCompat
import android.util.Log
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.geometry.Geometry
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import com.esri.arcgisruntime.mapping.Viewpoint

class ChangeViewPointActivity : AppCompatActivity() {
    private val SCALE = 7000
    private val TAG = "ChangeViewPoint"
    private var mMap: ArcGISMap? = null
    private var mMapView: MapView? = null
    private var spatialReference: SpatialReference? = null
    private var mGeometryButton: Button? = null
    private var mCenterScaleButton: Button? = null
    private var mAnimateButton: Button? = null
    private var mDuration = 10
    private var isGeometryButtonClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_viewpoint_activity)
        setTitle(R.string.title_change_viewpoint)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the BasemapType topographic
        mMap = ArcGISMap(Basemap.createImageryWithLabels())
        // set the map to be displayed in this view
        mMapView!!.map = mMap
        // create spatial reference for all points
        spatialReference = SpatialReference.create(2229)
        // create point for starting location - London
        val startPoint = Point(28677947.756181, 22987445.6186465, spatialReference)

        //set viewpoint of map view to starting point and scaled
        mMapView!!.setViewpointCenterAsync(startPoint, SCALE.toDouble())

        // inflate the Buttons from the layout
        mGeometryButton = findViewById(R.id.geometryButton) as Button
        mCenterScaleButton = findViewById(R.id.centerScaleButton) as Button
        mAnimateButton = findViewById(R.id.animateButton) as Button

        // create geometry of Griffith Park from JSON raw file, add graphics and set viewpoint of the map view to Griffith Park
        mGeometryButton!!.setOnClickListener {
            // create an input stream for the raw text file containing JSON of Griffith Park
            val ins = resources.openRawResource(
                    resources.getIdentifier("griffithparkjson","raw", packageName))

            val inputReader = InputStreamReader(ins)

            val bufferReader = BufferedReader(inputReader)
            var line: String
            val text = StringBuilder()

            // read the text file
            try {
                for(line in bufferReader.readLine())
                    if (line != null) text.append(line)
            } catch (e: IOException) {
                Log.d(TAG, e.toString())
            }

            val JsonString = text.toString()

            // create Geometry from JSON
            val geometry = Geometry.fromJson(JsonString, spatialReference)
            val overlay = GraphicsOverlay()
            // add graphics overlay on map view
            mMapView!!.graphicsOverlays.add(overlay)
            val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.GREEN, null)
            // add graphic of Griffith Park
            overlay.graphics.add(Graphic(geometry, fillSymbol))

            // set viewpoint of map view to Geometry - Griffith Park
            mMapView!!.setViewpointGeometryAsync(geometry)

            mGeometryButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary_dark))
            mAnimateButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))
            mCenterScaleButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))

            isGeometryButtonClicked = true
        }
        mAnimateButton!!.setOnClickListener {
            val scale: Int
            if (isGeometryButtonClicked) {
                scale = SCALE * SCALE
                isGeometryButtonClicked = false
            } else {
                scale = SCALE
            }
            // create the London location point
            val londonPoint = Point(28677947.756181, 22987445.6186465, spatialReference)
            // create the viewpoint with the London point and scale
            val viewpoint = Viewpoint(londonPoint, scale.toDouble())
            // set the map views's viewpoint to London with a ten second duration
            mMapView!!.setViewpointAsync(viewpoint, mDuration.toFloat())

            mGeometryButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))
            mAnimateButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary_dark))
            mCenterScaleButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))
        }
        //用lambad的写法可以省略写new和override
        mCenterScaleButton!!.setOnClickListener {
            // create the Waterloo location point
            val waterlooPoint = Point(28681235.9843606, 22990575.7224154, spatialReference)
            // set the map views's viewpoint centered on Waterloo and scaled
            mMapView!!.setViewpointCenterAsync(waterlooPoint, SCALE.toDouble())

            mGeometryButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))
            mAnimateButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary))
            mCenterScaleButton!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.primary_dark))
        }
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
        if (keyCode == KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }
}
