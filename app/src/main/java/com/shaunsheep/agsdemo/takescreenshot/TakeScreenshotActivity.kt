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

package com.shaunsheep.agsdemo.takescreenshot

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaActionSound
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class TakeScreenshotActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private val requestCode = 2
    private val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_take_screenshot)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView
        // create a map with the BasemapType Imagery with Labels
        val mMap = ArcGISMap(Basemap.createImageryWithLabels())
        // set the map to be displayed in this view
        mMapView!!.map = mMap
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.take_screenshot, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle menu item selection

        val itemId = item.itemId
        if (itemId == R.id.CaptureMap) {
            // Check permissions to see if failure may be due to lack of permissions.
            val permissionCheck = ContextCompat.checkSelfPermission(this@TakeScreenshotActivity, permission[0]) == PackageManager.PERMISSION_GRANTED

            if (!permissionCheck) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this@TakeScreenshotActivity, permission, requestCode)
            } else {
                captureScreenshotAsync()
            }
        }

        return true
    }

    /**
     * capture the map as an image
     */
    private fun captureScreenshotAsync() {

        // export the image from the mMapView
        val export = mMapView!!.exportImageAsync()
        export.addDoneListener {
            try {
                val currentMapImage = export.get()
                // play the camera shutter sound
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
                Log.d(TAG, "Captured the image!!")
                // save the exported bitmap to an image file
                val saveImageTask = SaveImageTask()
                saveImageTask.execute(currentMapImage)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, resources.getString(R.string.map_export_failure) + e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, resources.getString(R.string.map_export_failure) + e.message)
            }
        }
    }

    /**
     * save the bitmap image to file and open it

     * @param bitmap
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun saveToFile(bitmap: Bitmap): File {

        // create a directory ArcGIS to save the file
        val root: File
        var file: File? = null
        val fileName = "map-export-image" + System.currentTimeMillis() + ".png"
        root = Environment.getExternalStorageDirectory()
        val fileDir = File(root.absolutePath + "/ArcGIS Export/")
        var isDirectoryCreated = fileDir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = fileDir.mkdirs()
        }
        if (isDirectoryCreated) {
            file = File(fileDir, fileName)
            // write the bitmap to PNG file
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

            // close the stream
            fos.flush()
            fos.close()
        }
        return file!!

    }

    /**
     * AsyncTask class to save the bitmap as an image
     */
    private inner class SaveImageTask : AsyncTask<Bitmap, Void, File>() {

        override fun onPreExecute() {
            // display a toast message to inform saving the map as an image
            Toast.makeText(applicationContext, resources.getString(R.string.map_export_message), Toast.LENGTH_SHORT).show()
        }

        /**
         * save the file using a worker thread
         */
        override fun doInBackground(vararg mapBitmap: Bitmap): File? {

            try {
                return saveToFile(mapBitmap[0])
            } catch (e: Exception) {
                Log.e(TAG, resources.getString(R.string.map_export_failure) + e.message)
            }

            return null

        }

        /**
         * Perform the work on UI thread to open the exported map image
         */
        override fun onPostExecute(file: File) {
            // Open the file to view
            val i = Intent()
            i.action = Intent.ACTION_VIEW
            i.setDataAndType(Uri.fromFile(file), "image/png")
            startActivity(i)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            captureScreenshotAsync()
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(this@TakeScreenshotActivity, resources.getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show()

        }
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
    companion object {
        private val TAG = "TakeScreenshot"
    }

}
