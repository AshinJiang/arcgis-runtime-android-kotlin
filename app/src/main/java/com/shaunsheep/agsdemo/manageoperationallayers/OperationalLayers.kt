/* Copyright 2015 Esri
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

package com.shaunsheep.agsdemo.manageoperationallayers

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView

import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.LayerList
import com.shaunsheep.agsdemo.R
import com.shaunsheep.agsdemo.manageoperationallayers.listviewdragginganimation.DynamicListView
import com.shaunsheep.agsdemo.manageoperationallayers.listviewdragginganimation.StableArrayAdapter

import java.util.ArrayList
import java.util.Arrays

class OperationalLayers : AppCompatActivity() {

    private var mAddedLayerList: ArrayList<String>? = null
    private var mRemovedLayerList: ArrayList<String>? = null
    private var mMapOperationalLayers: LayerList? = null
    private val mRemovedLayers = ArrayList<Layer>()
    private var mOperationalLayerAdapter: StableArrayAdapter? = null
    private var mRemovedLayerAdapter: ArrayAdapter<String>? = null
    private var mOperationalLayersListViewId: Int = 0
    private var mRemovedLayerListViewId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_operational_layers_activity)
        setTitle(R.string.title_manage_operationlayer)

        // inflate Button from the layout
        val doneButton = findViewById(R.id.donebutton) as Button

        // listviewids to reuse one dialogbox between the two listview onItemClickListeners
        mOperationalLayersListViewId = resources.getIdentifier("dynamiclistview", "id", this.packageName)
        mRemovedLayerListViewId = resources.getIdentifier("listView", "id", this.packageName)

        // get the string array with the layer names from the strings resource file
        val layers = resources.getStringArray(R.array.operational_layer_array)

        // initialize the addedLayerList array with the names of the layer added initially to the Map
        mAddedLayerList = ArrayList(Arrays.asList(*layers))
        // removed layer list is initially blank
        mRemovedLayerList = ArrayList<String>()

        // get the map operational layers LayerList from the ManageOperationalLayerActivity
        mMapOperationalLayers = ManageOperationalLayerActivity.operationalLayerList
        // the sample maintains an arraylist with the Layer objects that have been removed from the Map,
        // so that they can be re-added to the map

        // initialize the adapter for the list of layer added to the Map
        mOperationalLayerAdapter = StableArrayAdapter(this, R.layout.manage_operation_layer_text_view, mAddedLayerList)
        // inflate the operationalLayers listview
        val operationalLayersListView = findViewById(R.id.dynamiclistview) as DynamicListView

        operationalLayersListView.setLayerList(mAddedLayerList)
        operationalLayersListView.adapter = mOperationalLayerAdapter
        operationalLayersListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        // initialize the adapter for the list of removed layers
        mRemovedLayerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mRemovedLayerList!!)

        // inflate the removedlayers listview
        val removedLayerListView = findViewById(R.id.listView) as ListView
        removedLayerListView.adapter = mRemovedLayerAdapter

        doneButton.setOnClickListener {
            // if there are two layers present in the added layers list
            // handle the cell swaping
            if (mAddedLayerList!!.size > 1) {
                // if the first item on the list it world elevations make sure that the
                // same layer is present in the LayerList of the Map
                if (mAddedLayerList!![0] == "World Elevations") {
                    if (mMapOperationalLayers!![0].name != "WorldElevations") {
                        // if not then swap the layer positons
                        val temp = mMapOperationalLayers!!.removeAt(0)
                        mMapOperationalLayers!!.add(1, temp)
                    }
                } else {
                    if (mMapOperationalLayers!![0].name != "Census") {
                        val temp = mMapOperationalLayers!!.removeAt(0)
                        mMapOperationalLayers!!.add(1, temp)
                    }
                }
            }

            // with the user selected options applied go back to the mainActivity
            val intent = Intent(this@OperationalLayers, ManageOperationalLayerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        // if the layer in the Added layers ListView is clicked on, handle it
        operationalLayersListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> showDialog(mOperationalLayersListViewId, position, "This layer will be removed, confirm?") }

        // if the layer in the removed layers ListView is clicked on, handle it
        removedLayerListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> showDialog(mRemovedLayerListViewId, position, "This layer will be added, confirm?") }

    }

    /**
     * removes the layer in the added layers list, and adds it to the removed layers list
     * both from the view and the LayerList

     * @param position of the layer to be removed in the removedLayerList
     */
    private fun removeLayer(position: Int) {
        mRemovedLayerList!!.add(mAddedLayerList!![position])
        mRemovedLayers.add(mMapOperationalLayers!!.removeAt(position))
        mAddedLayerList!!.removeAt(position)

        mRemovedLayerAdapter!!.notifyDataSetChanged()
        mOperationalLayerAdapter!!.notifyDataSetChanged()

    }

    /**
     * adds the layer to the added layers list, and removes it from the removed layers list

     * @param position of the layer to be added in the addedLayerList
     */
    private fun addLayer(position: Int) {
        mAddedLayerList!!.add(mRemovedLayerList!![position])
        mMapOperationalLayers!!.add(mRemovedLayers.removeAt(position))
        mRemovedLayerList!!.removeAt(position)

        mRemovedLayerAdapter!!.notifyDataSetChanged()
        mOperationalLayerAdapter!!.notifyDataSetChanged()
    }


    /**
     * shows the dialog box

     * @param listViewId to determine which ListView the user clicked on
     * *
     * @param position   position of the item in the List
     * *
     * @param message    title of the dialog box
     */
    private fun showDialog(listViewId: Int, position: Int, message: String) {

        val alertDialogBuilder = AlertDialog.Builder(
                this@OperationalLayers)

        // set title
        alertDialogBuilder.setTitle("manage-operational-layers")

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // if the list view id is for OperationalLayersListView then
                    // remove the layer
                    if (listViewId == mOperationalLayersListViewId) {
                        removeLayer(position)
                    } else {
                        // add the layer
                        addLayer(position)
                    }
                }
                .setNegativeButton("No") { dialog, id ->
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel()
                }

        // create alert dialog
        val alertDialog = alertDialogBuilder.create()

        // show it
        alertDialog.show()

    }

}
