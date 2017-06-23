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

package com.shaunsheep.agsdemo.openmobilemappackage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast

import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

import java.io.File

class OpenMapPackageActivity : AppCompatActivity() {
    private var mMapView: MapView? = null
    private var mapPackage: MobileMapPackage? = null

    // define permission to request
    internal var reqPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val requestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_open_mmpk)

        // get sdcard resource name
        extStorDir = Environment.getExternalStorageDirectory()
        // get the directory
        extSDCardDirName = this.resources.getString(R.string.config_data_sdcard_offline_mmpk)
        // get mobile map package filename
        filename = this.resources.getString(R.string.config_mmpk_name)
        // create the full path to the mobile map package file
        mmpkFilePath = createMobileMapPackageFilePath()

        // retrieve the MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(this@OpenMapPackageActivity, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            loadMobileMapPackage(mmpkFilePath!!)
        } else {
            // request permission
            ActivityCompat.requestPermissions(this@OpenMapPackageActivity, reqPermission, requestCode)
        }

    }

    /**
     * Handle the permissions request response

     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMobileMapPackage(mmpkFilePath!!)
        } else {
            // report to user that permission was denied
            Toast.makeText(this@OpenMapPackageActivity, resources.getString(R.string.open_mmpk_location_permission_denied),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Load a mobile map package into a MapView

     * @param mmpkFile Full path to mmpk file
     */
    private fun loadMobileMapPackage(mmpkFile: String) {
        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        mapPackage = MobileMapPackage(mmpkFile)
        // load the mobile map package asynchronously
        mapPackage!!.loadAsync()

        // add done listener which will invoke when mobile map package has loaded
        mapPackage!!.addDoneLoadingListener {
            // check load status and that the mobile map package has maps
            if (mapPackage!!.loadStatus == LoadStatus.LOADED && mapPackage!!.maps.size > 0) {
                // add the map from the mobile map package to the MapView
                mMapView!!.map = mapPackage!!.maps[0]
            } else {
                // Log an issue if the mobile map package fails to load
                Log.e(TAG, mapPackage!!.loadError.message)
            }
        }
        //[DocRef: END]
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

        private val TAG = "MMPK"
        private val FILE_EXTENSION = ".mmpk"
        private var extStorDir: File? = null
        private var extSDCardDirName: String? = null
        private var filename: String? = null
        private var mmpkFilePath: String? = null

        /**
         * Create the mobile map package file location and name structure
         */
        private fun createMobileMapPackageFilePath(): String {
            return extStorDir!!.absolutePath + File.separator + extSDCardDirName + File.separator + filename + FILE_EXTENSION
        }
    }
}
