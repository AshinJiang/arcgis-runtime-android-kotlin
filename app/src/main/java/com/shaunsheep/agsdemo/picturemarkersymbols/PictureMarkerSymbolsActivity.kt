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

package com.shaunsheep.agsdemo.picturemarkersymbols

import java.io.File
import java.io.FileOutputStream

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast

import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.shaunsheep.agsdemo.R

class PictureMarkerSymbolsActivity : AppCompatActivity() {

    private var mMapView: MapView?=null
    private var mGraphicsOverlay: GraphicsOverlay?=null

    private var mArcGISTempFolderPath: String?=null
    private var mPinBlankOrangeFilePath: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_picture_marker)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the imagery basemap
        val map = ArcGISMap(Basemap.createTopographic())

        // set the map to be displayed in the mapview
        mMapView!!.map = map

        // create an initial viewpoint using an envelope (of two points, bottom left and top right)
        val envelope = Envelope(Point(-228835.0, 6550763.0, SpatialReferences.getWebMercator()),
                Point(-223560.0, 6552021.0, SpatialReferences.getWebMercator()))
        //set viewpoint on mapview
        mMapView!!.setViewpointGeometryAsync(envelope, 100.0)

        // create a new graphics overlay and add it to the mapview
        mGraphicsOverlay = GraphicsOverlay()
        mMapView!!.graphicsOverlays.add(mGraphicsOverlay)

        //[DocRef: Name=Picture Marker Symbol URL, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from a URL resource
        //When using a URL, you need to call load to fetch the remote resource
        val campsiteSymbol = PictureMarkerSymbol(
                "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae")
        //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
        //its appearance would then differ across devices with different resolutions.
        campsiteSymbol.height = 18f
        campsiteSymbol.width = 18f
        campsiteSymbol.loadAsync()
        //[DocRef: END]
        campsiteSymbol.addDoneLoadingListener {
            //Once the symbol has loaded, add a new graphic to the graphic overlay
            val campsitePoint = Point(-223560.0, 6552021.0, SpatialReferences.getWebMercator())
            val campsiteGraphic = Graphic(campsitePoint, campsiteSymbol)
            mGraphicsOverlay!!.graphics.add(campsiteGraphic)
        }

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        val pinStarBlueDrawable = ContextCompat.getDrawable(this, R.mipmap.pin_star_blue) as BitmapDrawable
        val pinStarBlueSymbol = PictureMarkerSymbol(pinStarBlueDrawable)
        //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
        //its appearance would then differ across devices with different resolutions.
        pinStarBlueSymbol.height = 40f
        pinStarBlueSymbol.width = 40f
        //Optionally set the offset, to align the base of the symbol aligns with the point geometry
        pinStarBlueSymbol.offsetY = 11f //The image used for the symbol has a transparent buffer around it, so the offset is not simply height/2
        pinStarBlueSymbol.loadAsync()
        //[DocRef: END]
        pinStarBlueSymbol.addDoneLoadingListener {
            //add a new graphic with the same location as the initial viewpoint
            val pinStarBluePoint = Point(-226773.0, 6550477.0, SpatialReferences.getWebMercator())
            val pinStarBlueGraphic = Graphic(pinStarBluePoint, pinStarBlueSymbol)
            mGraphicsOverlay!!.graphics.add(pinStarBlueGraphic)
        }

        //see createPictureMarkerSymbolFromFile() method for implementation
        //first run checks for external storage and permissions,
        checkSaveResourceToExternalStorage()

    }

    /**
     * Create a picture marker symbol from an image on disk. Called from checkSaveResourceToExternalStorage() or
     * onRequestPermissionsResult which validate required external storage and permissions
     */
    private fun createPictureMarkerSymbolFromFile() {

        //[DocRef: Name=Picture Marker Symbol File-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from a file on disk
        val pinBlankOrangeDrawable = Drawable.createFromPath(mPinBlankOrangeFilePath) as BitmapDrawable
        val pinBlankOrangeSymbol = PictureMarkerSymbol(pinBlankOrangeDrawable)
        //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
        //its appearance would then differ across devices with different resolutions.
        pinBlankOrangeSymbol.height = 20f
        pinBlankOrangeSymbol.width = 20f
        //Optionally set the offset, to align the base of the symbol aligns with the point geometry
        pinBlankOrangeSymbol.offsetY = 10f //The image used has not buffer and therefore the Y offset is height/2
        pinBlankOrangeSymbol.loadAsync()
        //[DocRef: END]
        pinBlankOrangeSymbol.addDoneLoadingListener {
            //add a new graphic with the same location as the initial viewpoint
            val pinBlankOrangePoint = Point(-228835.0, 6550763.0, SpatialReferences.getWebMercator())
            val pinBlankOrangeGraphic = Graphic(pinBlankOrangePoint, pinBlankOrangeSymbol)
            mGraphicsOverlay!!.graphics.add(pinBlankOrangeGraphic)
        }

    }

    /**
     * Helper method to save an image which is within this sample as a drawable resource to the sdcard so that it can be
     * used as the basis of a PictureMarkerSymbol created from a file on disc
     */
    private fun checkSaveResourceToExternalStorage() {

        //first, check if there is no sdcard
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            // no mounted disc, cannot proceed
            return
        }

        //Check for required permission of saving to disc, for devices < android 6 this is set in the manifest and should be granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //no permission, need to task, onRequestPermissionsResult will handle the result
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            //permission granted, proceed
            //save the orange marker app resource to disk
            if (saveFileToExternalStorage()) {
                createPictureMarkerSymbolFromFile()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                //If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    if (saveFileToExternalStorage()) {
                        createPictureMarkerSymbolFromFile()
                    }
                }
            }
        }
    }

    private fun saveFileToExternalStorage(): Boolean {

        //build paths
        mArcGISTempFolderPath = Environment.getExternalStorageDirectory().toString() + File.separator + this.resources
                .getString(R.string.pin_blank_orange_folder_name)
        mPinBlankOrangeFilePath = mArcGISTempFolderPath + File.separator + this.resources.getString(R.string.pin_blank_orange_file_name)

        //get drawable resource
        val bm = BitmapFactory.decodeResource(resources, R.mipmap.pin_blank_orange)

        //create new ArcGIS temp folder
        val folder = File(mArcGISTempFolderPath)
        if (folder.mkdirs()) {
            Log.d(TAG, "Temp folder created")
        } else {
            Toast.makeText(this@PictureMarkerSymbolsActivity, "Could not create temp folder", Toast.LENGTH_LONG).show()
        }

        //create file on disk
        val file = File(mPinBlankOrangeFilePath)

        try {
            val outStream = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()

            return true

        } catch (e: Exception) {
            Log.e("picture-marker-symbol", "Failed to write image to external directory: message = " + e.message)
            return false
        }

    }

    public override fun onDestroy() {
        super.onDestroy()
        //Clean up file and folders we saved to disk
        try {
            val file = File(mPinBlankOrangeFilePath)

            if (file.delete()) {
                Log.d(TAG, "Temp folder created")
            } else {
                Toast.makeText(this@PictureMarkerSymbolsActivity, "Could not create temp folder", Toast.LENGTH_LONG).show()
            }

            val tempFolder = File(mArcGISTempFolderPath)

            if (tempFolder.delete()) {
                Log.d(TAG, "Temp folder created")
            } else {
                Toast.makeText(this@PictureMarkerSymbolsActivity, "Could not create temp folder", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("picture-marker-symbol",
                    "Failed to delete temp files and directory written to external storage: message = " + e.message)
        }

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

    companion object {

        private val TAG = "PictureMarkerSymbols"

        private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101
    }
}
