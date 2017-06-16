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

package com.shaunsheep.agsdemo.featurelayergeodatabase

import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Toast

import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R

class FeatureLayerGeodatabaseActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mGeodatabase: Geodatabase? = null

    // define permission to request
    private val reqPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_featurelayer_Gdb)

        // create the path to local data
        extStorDir = Environment.getExternalStorageDirectory()
        extSDCardDirName = this.resources.getString(R.string.config_data_sdcard_offline_flgdb)
        vtpkFilename = this.resources.getString(R.string.config_vtpk_name)
        geodbFilename = this.resources.getString(R.string.config_geodb_name)

        // full path to data
        mVtpk = createvtpkFilePath()
        mGeoDb = createGeoDbFilePath()

        // create MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(this@FeatureLayerGeodatabaseActivity, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            addData(mVtpk!!, mGeoDb!!)
        } else {
            // request permission
            val requestCode = 2
            ActivityCompat.requestPermissions(this@FeatureLayerGeodatabaseActivity, reqPermission, requestCode)
        }

    }

    /**
     * Handle the permissions request response
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addData(mVtpk!!, mGeoDb!!)
        } else {
            // report to user that permission was denied
            Toast.makeText(this@FeatureLayerGeodatabaseActivity, resources.getString(R.string.store_write_permission_denied),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Load a vector tile file into a MapView

     * @param vtpkFile  Full path to vector tile layer package file
     * *
     * @param geoDbFile Full path to geodatabase file
     */
    private fun addData(vtpkFile: String, geoDbFile: String) {
        // create a new ArcGISVectorTiledLayer from local path
        val vectorTiledLayer = ArcGISVectorTiledLayer(vtpkFile)
        // create a Basemap instance for use in creating an ArcGISMap instance
        val basemap = Basemap(vectorTiledLayer)
        val mArcGISMap = ArcGISMap(basemap)
        // set the mArcGISMap to be displayed in this view
        mMapView!!.map = mArcGISMap
        // create a new Geodatabase from local path
        mGeodatabase = Geodatabase(geoDbFile)
        // load the geodatabase
        mGeodatabase!!.loadAsync()
        // add feature layer from geodatabase to the ArcGISMap
        mGeodatabase!!.addDoneLoadingListener {
            for (geoDbTable in mGeodatabase!!.geodatabaseFeatureTables) {
                mMapView!!.map.operationalLayers.add(FeatureLayer(geoDbTable))
            }
        }

        // set initial viewpoint once MapView has spatial reference
        mMapView!!.addSpatialReferenceChangedListener {
            // set the initial viewpoint
            val initPnt = Point(-13214155.0, 4040194.0, SpatialReference.create(3857))
            mMapView!!.setViewpoint(Viewpoint(initPnt, 35e4))
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

        private var extStorDir: File? = null
        private var extSDCardDirName: String? = null
        private var vtpkFilename: String? = null
        private var geodbFilename: String? = null
        private var mVtpk: String? = null
        private var mGeoDb: String? = null

        /**
         * Create the vector tile layer file location and name structure
         */
        private fun createvtpkFilePath(): String {
            return extStorDir!!.absolutePath + File.separator + extSDCardDirName + File.separator + vtpkFilename
        }

        /**
         * Create the mobile geodatabase file location and name structure
         */
        private fun createGeoDbFilePath(): String {
            return extStorDir!!.absolutePath + File.separator + extSDCardDirName + File.separator + geodbFilename
        }
    }
}
