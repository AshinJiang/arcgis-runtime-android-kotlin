/* Copyright 2017 Esri
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

package com.shaunsheep.agsdemo.blendrenderer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.raster.BlendRenderer
import com.esri.arcgisruntime.raster.ColorRamp
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.SlopeType
import com.shaunsheep.agsdemo.R

import java.io.File

class BlendRendererActivity : AppCompatActivity(), ParametersDialogFragment.ParametersListener {

    private var mMapView: MapView? = null
    private var mImageFile: File? = null
    private var mElevationFile: File? = null

    private var mAltitude: Int = 0
    private var mAzimuth: Int = 0
    private var mZFactor: Double = 0.toDouble()
    private var mSlopeType: SlopeType? = null
    private var mColorRampType: ColorRamp.PresetType? = null
    private var mPixelSizeFactor: Double = 0.toDouble()
    private var mPixelSizePower: Double = 0.toDouble()
    private var mOutputBitDepth: Int = 0

    private var mFragmentManager: FragmentManager? = null

    override fun returnParameters(altitude: Int, azimuth: Int, slopeType: SlopeType, colorRampType: ColorRamp.PresetType) {
        //gets dialog box parameters and calls updateRenderer
        mAltitude = altitude
        mAzimuth = azimuth
        mSlopeType = slopeType
        mColorRampType = colorRampType
        updateRenderer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blend_renderer_activity)
        setTitle(R.string.title_blend_renderer)
        //set default values for blend parameters
        mAltitude = 45
        mAzimuth = 315
        mZFactor = 0.000016
        mSlopeType = SlopeType.NONE
        mColorRampType = ColorRamp.PresetType.NONE
        mPixelSizeFactor = 1.0
        mPixelSizePower = 1.0
        mOutputBitDepth = 8
        // retrieve the MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        mFragmentManager = supportFragmentManager
        // define permission to request
        val reqPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val requestCode = 2
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(applicationContext,
                reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            blendRenderer()
        } else {
            // request permission
            ActivityCompat.requestPermissions(this@BlendRendererActivity, reqPermission, requestCode)
        }
    }

    /**
     * Using values stored in strings.xml, builds path to rasters.

     * @return the path to raster file
     */
    private fun buildRasterPath(filename: String): String {
        // get sdcard resource name
        val extStorDir = Environment.getExternalStorageDirectory()
        // get the directory
        val extSDCardDirName = this.resources.getString(R.string.data_sdcard_offline_dir)
        // create the full path to the raster file
        return extStorDir.absolutePath+File.separator+extSDCardDirName+File.separator+filename+".tif"
    }

    /**
     * Creates new imagery and elevation files based on a given path, creates an ArcGISMap, sets it to a MapView and
     * calls updateRenderer().
     */
    private fun blendRenderer() {
        // create raster files
        mImageFile = File(buildRasterPath(this.getString(R.string.imagery_raster_name)))
        mElevationFile = File(buildRasterPath(this.getString(R.string.elevation_raster_name)))
        // create a map
        val map = ArcGISMap()
        // add the map to a map view
        mMapView!!.map = map
        updateRenderer()
    }

    /**
     * Creates ColorRamp and BlendRenderer according to the chosen property values.
     */
    private fun updateRenderer() {
        // if color ramp type is not None, create a new ColorRamp
        val colorRamp = if (mColorRampType != ColorRamp.PresetType.NONE) ColorRamp(mColorRampType!!, 800) else
            null
        // create rasters
        val imageryRaster = Raster(mImageFile!!.absolutePath)
        val elevationRaster = Raster(mElevationFile!!.absolutePath)
        // if color ramp is not NONE, color the hillshade elevation raster instead of using satellite imagery raster color
        val rasterLayer = if (colorRamp != null) RasterLayer(elevationRaster) else RasterLayer(imageryRaster)
        mMapView!!.map.basemap = Basemap(rasterLayer)
        // create blend renderer
        val blendRenderer = BlendRenderer(
                elevationRaster,
                listOf(9.0),
                listOf(255.0), null, null, null, null,
                colorRamp,
                mAltitude.toDouble(),
                mAzimuth.toDouble(),
                mZFactor,
                mSlopeType!!,
                mPixelSizeFactor,
                mPixelSizePower,
                mOutputBitDepth)
        rasterLayer.rasterRenderer = blendRenderer
    }

    /**
     * Handle the permissions request response.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            blendRenderer()
        } else {
            // report to user that permission was denied
            Toast.makeText(applicationContext,
                    resources.getString(R.string.store_write_permission_denied),
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.blend_parameters, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val paramDialog = ParametersDialogFragment()
        val blendParameters = Bundle()
        //send parameters to fragment
        blendParameters.putInt("altitude", mAltitude)
        blendParameters.putInt("azimuth", mAzimuth)
        blendParameters.putSerializable("slope_type", mSlopeType)
        blendParameters.putSerializable("color_ramp_type", mColorRampType)
        paramDialog.arguments = blendParameters
        paramDialog.show(mFragmentManager!!, "param_dialog")
        return super.onOptionsItemSelected(item)
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
        if (keyCode==KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}