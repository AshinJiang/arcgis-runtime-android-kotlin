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

package com.shaunsheep.agsdemo.uniquevaluerenderer

import java.util.ArrayList
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.shaunsheep.agsdemo.R


class UniqueValueRendererActivity : AppCompatActivity() {

    private var mMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_unique_value_renderer)

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the topographic basemap
        val map = ArcGISMap(Basemap.createTopographic())

        //[DocRef: Name=Unique Value Renderer, Topic=Symbols and Renderers, Category=Fundamentals]
        // Create service feature table
        val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.census_service_url))

        // Create the feature layer using the service feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)

        // Override the renderer of the feature layer with a new unique value renderer
        val uniqueValueRenderer = UniqueValueRenderer()
        // Set the field to use for the unique values
        uniqueValueRenderer.fieldNames.add("STATE_ABBR") //You can add multiple fields to be used for the renderer in the form of a list, in this case we are only adding a single field

        // Create the symbols to be used in the renderer
        val defaultFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.BLACK, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GRAY, 2f))
        val californiaFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f))
        val arizonaFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2f))
        val nevadaFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2f))

        // Set default symbol
        uniqueValueRenderer.defaultSymbol = defaultFillSymbol
        uniqueValueRenderer.defaultLabel = "Other"

        // Set value for california
        val californiaValue = ArrayList<Any>()
        // You add values associated with fields set on the unique value renderer.
        // If there are multiple values, they should be set in the same order as the fields are set
        californiaValue.add("CA")
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue("California", "State of California", californiaFillSymbol, californiaValue))

        // Set value for arizona
        val arizonaValue = ArrayList<Any>()
        // You add values associated with fields set on the unique value renderer.
        // If there are multiple values, they should be set in the same order as the fields are set
        arizonaValue.add("AZ")
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue("Arizona", "State of Arizona", arizonaFillSymbol, arizonaValue))

        // Set value for nevada
        val nevadaValue = ArrayList<Any>()
        // You add values associated with fields set on the unique value renderer.
        // If there are multiple values, they should be set in the same order as the fields are set
        nevadaValue.add("NV")
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue("Nevada", "State of Nevada", nevadaFillSymbol, nevadaValue))

        // Set the renderer on the feature layer
        featureLayer.renderer = uniqueValueRenderer
        //[DocRef: END]

        // add the layer to the map
        map.operationalLayers.add(featureLayer)

        map.initialViewpoint = Viewpoint(Envelope(-13893029.0, 3573174.0, -12038972.0, 5309823.0, SpatialReferences.getWebMercator()))

        // set the map to be displayed in the mapview
        mMapView!!.map = map

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
}
