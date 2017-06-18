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

package com.shaunsheep.agsdemo.managebookmarks

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.mapping.*
import com.esri.arcgisruntime.mapping.view.MapView
import com.shaunsheep.agsdemo.R
import java.util.*

class ManagerBookmarksActivity : AppCompatActivity() {

    private var mMapView: MapView? = null
    private var mBookmarks: BookmarkList? = null
    private var mBookmarksSpinnerList: MutableList<String>? = null
    private var mBookmark: Bookmark? = null
    private var mDataAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_bookmarks_activity)
        setTitle(R.string.title_manager_bookmarks)

        val addBookmarkFab: FloatingActionButton

        val bookmarksSpinner: Spinner

        // inflate MapView from layout
        mMapView = findViewById(R.id.mapView) as MapView

        // create a map with the BasemapType imagery with labels
        val mMap = ArcGISMap(Basemap.createImageryWithLabels())
        // set the map to be displayed in this view
        mMapView!!.map = mMap

        // inflate the floating action button
        addBookmarkFab = findViewById(R.id.addbookmarkFAB) as FloatingActionButton

        // show the dialog for acquiring bookmark name from the user
        addBookmarkFab.setOnClickListener { v -> showDialog(v.context) }

        // get the maps BookmarkList
        mBookmarks = mMap.bookmarks

        // add some default bookmarks to the map
        addDefaultBookmarks()

        // populate the spinner list with default bookmark names
        bookmarksSpinner = findViewById(R.id.bookmarksspinner) as Spinner
        mBookmarksSpinnerList = ArrayList<String>()
        mBookmarksSpinnerList!!.add(mBookmarks!![0].name)
        mBookmarksSpinnerList!!.add(mBookmarks!![1].name)
        mBookmarksSpinnerList!!.add(mBookmarks!![2].name)
        mBookmarksSpinnerList!!.add(mBookmarks!![3].name)

        // initialize the adapter for the bookmarks spinner
        mDataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mBookmarksSpinnerList!!)
        mDataAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bookmarksSpinner.adapter = mDataAdapter

        // when an item is selected in the spinner set the mapview viewpoint to the selected
        // bookmark's viewpoint
        bookmarksSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mMapView!!.setViewpointAsync(mBookmarks!![position].viewpoint)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * adds the default bookmarks to the maps BookmarkList
     */
    private fun addDefaultBookmarks() {

        var viewpoint: Viewpoint

        //Mysterious Desert Pattern
        viewpoint = Viewpoint(27.3805833, 33.6321389, 6e3)
        mBookmark = Bookmark(resources.getString(R.string.desert_pattern), viewpoint)
        mBookmarks!!.add(mBookmark)
        // Set the viewpoint to the default bookmark selected in the spinner
        mMapView!!.setViewpointAsync(viewpoint)

        //Strange Symbol
        viewpoint = Viewpoint(37.401573, -116.867808, 6e3)
        mBookmark = Bookmark(resources.getString(R.string.strange_symbol), viewpoint)
        mBookmarks!!.add(mBookmark)

        //Guitar-Shaped Trees
        viewpoint = Viewpoint(-33.867886, -63.985, 4e4)
        mBookmark = Bookmark(resources.getString(R.string.guitar_trees), viewpoint)
        mBookmarks!!.add(mBookmark)

        //Grand Prismatic Spring
        viewpoint = Viewpoint(44.525049, -110.83819, 6e3)
        mBookmark = Bookmark(resources.getString(R.string.prismatic_spring), viewpoint)
        mBookmarks!!.add(mBookmark)

    }

    /**
     * add a new bookmark at the location being displayed in the MapView's current Viewpoint
     * @param Name of the new bookmark
     */
    private fun addBookmark(Name: String) {

        mBookmark = Bookmark(Name, mMapView!!.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY))
        mBookmarks!!.add(mBookmark)
        mBookmarksSpinnerList!!.add(Name)
        mDataAdapter!!.notifyDataSetChanged()
    }

    /**
     * shows dialog that prompts user to add a name for the new Bookmark
     */
    private fun showDialog(context: Context) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert_dialog_title))

        // Set up the input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, which ->
            // get the input from EditText
            val bookmarkName = input.text.toString()
            // check if EditText is not empty & bookmark name has not been used
            if (bookmarkName.length > 0 && !mBookmarksSpinnerList!!.contains(bookmarkName)) {
                addBookmark(bookmarkName)
            } else {
                // display toast explaining bookmark not set
                Toast.makeText(applicationContext, resources.getString(R.string.bookmark_not_saved), Toast.LENGTH_LONG).show()
                dialog.cancel()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()

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
        if(keyCode==KeyEvent.KEYCODE_BACK) finish()
        return super.onKeyDown(keyCode, event)
    }

}