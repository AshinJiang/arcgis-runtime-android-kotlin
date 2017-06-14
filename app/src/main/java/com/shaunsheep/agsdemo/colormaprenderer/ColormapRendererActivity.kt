package com.shaunsheep.agsdemo.colormaprenderer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Toast
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.ColormapRenderer
import com.esri.arcgisruntime.raster.Raster
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.colormap_renderer_activity.*
import java.io.File

class ColormapRendererActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.colormap_renderer_activity)
        setTitle(R.string.title_colormap_renderer)

        // define permission to request
        val reqPermission = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val requestCode = 2
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(this,
                reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            loadRaster()
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, reqPermission, requestCode)
        }
    }

    /**
     * Loads ShastaBW.tif as a Raster and adds it to a new RasterLayer. RasterLayer is then added to the map as an
     * operational layer. A List of color values is created (0-149: red) (150-250: yellow). The List is passed to a new
     * ColorMapRenderer, which is then set to the RasterLayer Rendererer. Map viewpoint is then set based on Raster
     * geometry.
     */
    private fun loadRaster() {
        // create a raster from a local raster file
        val raster = Raster(buildRasterPath())
        // create a raster layer
        val rasterLayer = RasterLayer(raster)
        // create a Map with imagery basemap
        val map = ArcGISMap(Basemap.createImagery())
        // add the map to a map view
        mapView.map=map
        // add the raster as an operational layer
        map.operationalLayers.add(rasterLayer)
        // create a color map where values 0-149 are red (Color.RED) and 150-250 are yellow (Color.Yellow)
        val colors = ArrayList<Int>()
        for (i in 0..250) {
            if (i < 150) {
                colors.add(i, Color.RED)
            } else {
                colors.add(i, Color.YELLOW)
            }
        }
        // create a colormap renderer
        val colormapRenderer = ColormapRenderer(colors)
        // set the ColormapRenderer on the RasterLayer
        rasterLayer.rasterRenderer = colormapRenderer
        // set Viewpoint on the Raster
        rasterLayer.addDoneLoadingListener { mapView.setViewpointGeometryAsync(rasterLayer.fullExtent, 50.0) }
    }
    /**
     * Using values stored in strings.xml, builds path to ShastaBW.tif.

     * @return the path to raster file
     */
    private fun buildRasterPath(): String {
        // get sdcard resource name
        val extStorDir = Environment.getExternalStorageDirectory()
        // get the directory
        val extSDCardDirName = this.resources.getString(R.string.config_data_sdcard_offline_dir)
        // get raster filename
        val filename = this.getString(R.string.config_raster_name)
        // create the full path to the raster file
        return extStorDir.absolutePath + File.separator + extSDCardDirName + File.separator + filename + ".tif"
    }

    /**
     * Handle the permissions request response.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadRaster()
        } else {
            // report to user that permission was denied
            Toast.makeText(this,
                    resources.getString(R.string.colormap_location_permission_denied),
                    Toast.LENGTH_SHORT).show()
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
