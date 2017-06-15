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

package com.shaunsheep.agsdemo.editfeatureattachments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.shaunsheep.agsdemo.R
import com.shaunsheep.agsdemo.editfeatureattachments.arrayadapter.CustomList
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import org.apache.commons.io.FileUtils;

class EditAttachmentActivity : AppCompatActivity() {
    companion object {

        private val TAG = "EditAttachmentActivity"
        private val RESULT_LOAD_IMAGE = 1
    }
    private var adapter: CustomList?=null
    private var noOfAttachments: Int = 0
    private var addAttachmentFab: FloatingActionButton?=null
    private var requestCodeFolder = 2
    private var requestCodeGallery = 3
    private var permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var attachments: List<Attachment>? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var mServiceFeatureTable: ServiceFeatureTable? = null
    private var mAttributeID: String? = null
    private var list: ListView? = null
    private var attachmentList = ArrayList<String>()
    private var progressDialog: ProgressDialog? = null
    private var builder: AlertDialog.Builder? = null
    private var permissionsGranted = false
    private var listPosition: Int = 0
    private var listView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_feature_attachments)
        setTitle(R.string.title_edit_feature_attach)

        val bundle = intent.extras
        val s = bundle.getString(application.getString(R.string.attribute))
        noOfAttachments = bundle.getInt(application.getString(R.string.noOfAttachments))

        // Build a alert dialog with specified style
        builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)

        // inflate the floating action button
        addAttachmentFab = findViewById(R.id.addAttachmentFAB) as FloatingActionButton

        // select an image to upload as an attachment
        addAttachmentFab!!.setOnClickListener {
            if (!permissionsGranted) {
                getPermissions(requestCodeGallery)
            } else {
                selectAttachment()
            }
        }

        mServiceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))

        progressDialog = ProgressDialog(this)

        // display progress dialog if selected feature has attachments
        if (noOfAttachments != 0) {
            progressDialog!!.setTitle(application.getString(R.string.fetching_attachments))
            progressDialog!!.setMessage(application.getString(R.string.wait))

            progressDialog!!.show()
        } else {
            Toast.makeText(this@EditAttachmentActivity, application.getString(R.string.empty_attachment_message), Toast.LENGTH_LONG).show()
        }

        // inflate the list view
        list = findViewById(R.id.listView) as ListView
        // create custom adapter
        adapter = CustomList(this@EditAttachmentActivity, attachmentList)
        // set custom adapter on the list
        list!!.adapter = adapter
        fetchAttachmentsFromServer(s)

        // listener on attachment items to download the attachment
        list!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            listPosition = position
            listView = view
            if (!permissionsGranted) {
                getPermissions(requestCodeFolder)
            } else {
                fetchAttachmentAsync(position, view)
            }
        }


        //set onlong click listener to delete the attachment
        list!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { arg0, arg1, pos, id ->
            builder!!.setMessage(application.getString(R.string.delete_query))
            builder!!.setCancelable(true)

            builder!!.setPositiveButton(resources.getString(R.string.yes)) { dialog, id ->
                deleteAttachment(pos)
                dialog.dismiss()
            }
            builder!!.setNegativeButton(resources.getString(R.string.no)) { dialog, id -> dialog.cancel() }
            val alert = builder!!.create()
            alert.show()
            true
        }
    }

    private fun fetchAttachmentAsync(position: Int, view: View) {

        progressDialog!!.setTitle(application.getString(R.string.downloading_attachments))
        progressDialog!!.setMessage(application.getString(R.string.wait))
        progressDialog!!.show()

        // create a listenableFuture to fetch the attachment asynchronously
        val listenableFuture = attachments!![position].fetchDataAsync()
        listenableFuture.addDoneListener {
            try {
                val fileName = attachmentList[position]
                // create a drawable from InputStream
                val d = Drawable.createFromStream(listenableFuture.get(), fileName)
                // create a bitmap from drawable
                val bitmap = (d as BitmapDrawable).bitmap
                val root = Environment.getExternalStorageDirectory()
                val fileDir = File(root.absolutePath + "/ArcGIS/Attachments")
                // create folder /ArcGIS/Attachments in external storage
                var isDirectoryCreated = fileDir.exists()
                if (!isDirectoryCreated) {
                    isDirectoryCreated = fileDir.mkdirs()
                }
                var file: File? = null
                if (isDirectoryCreated) {
                    file = File(fileDir, fileName)
                    val fos = FileOutputStream(file)
                    // compress the bitmap to PNG format
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
                    fos.flush()
                    fos.close()
                }

                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
                // open the file in gallery
                val i = Intent()
                i.action = Intent.ACTION_VIEW
                i.setDataAndType(Uri.fromFile(file), "image/png")
                startActivity(i)

            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    /**
     * Delete the attachment from the feature

     * @param pos position of the attachment in the list view to be deleted
     */
    private fun deleteAttachment(pos: Int) {
        progressDialog!!.setTitle(application.getString(R.string.deleting_attachments))
        progressDialog!!.setMessage(application.getString(R.string.wait))
        progressDialog!!.show()

        val deleteResult = mSelectedArcGISFeature!!.deleteAttachmentAsync(attachments!![pos])
        attachmentList.removeAt(pos)
        adapter!!.notifyDataSetChanged()

        deleteResult.addDoneListener {
            val tableResult = mServiceFeatureTable!!.updateFeatureAsync(mSelectedArcGISFeature!!)
            // apply changes back to the server
            tableResult.addDoneListener { applyServerEdits() }
        }
    }


    /**
     * Asynchronously fetch the attachments to view as a list

     * @param objectID
     */
    private fun fetchAttachmentsFromServer(objectID: String) {
        attachmentList = ArrayList<String>()
        // create objects required to do a selection with a query
        val query = QueryParameters()
        // set the where clause of the query
        query.whereClause = "OBJECTID = " + objectID

        // query the feature table
        val future = mServiceFeatureTable!!.queryFeaturesAsync(query)

        future.addDoneListener {
            try {
                val result = future.get()
                val feature = result.iterator().next()
                mSelectedArcGISFeature = feature as ArcGISFeature
                // get the number of attachments
                val attachmentResults = mSelectedArcGISFeature!!.fetchAttachmentsAsync()
                attachmentResults.addDoneListener {
                    try {

                        attachments = attachmentResults.get()
                        // if selected feature has attachments, display them in a list fashion
                        if (!attachments!!.isEmpty()) {
                            //
                            for (attachment in attachments!!) {
                                attachmentList.add(attachment.name)
                            }

                            runOnUiThread {
                                if (progressDialog!!.isShowing) {
                                    progressDialog!!.dismiss()
                                }
                                adapter = CustomList(this@EditAttachmentActivity, attachmentList)
                                list!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

                        }


                    } catch (e: Exception) {
                        Log.e(TAG, e.message)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }


    }


    /**
     * Open Gallery to select an image as an attachment
     */

    private fun getPermissions(requestCode: Int) {
        val permissionCheck = ContextCompat.checkSelfPermission(this@EditAttachmentActivity, permission[0]) == PackageManager.PERMISSION_GRANTED

        if (!permissionCheck) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(this@EditAttachmentActivity, permission, requestCode)

        } else {
            permissionsGranted = true
            if (requestCode == requestCodeGallery) {
                selectAttachment()
            } else {
                fetchAttachmentAsync(listPosition, listView!!)
            }

        }
    }

    private fun selectAttachment() {

        val i = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(i, RESULT_LOAD_IMAGE)

    }

    /**
     * Upload the selected image from the gallery as an attachment to the selected feature

     * @param requestCode RESULT_LOAD_IMAGE request code to identify the requesting activity
     * *
     * @param resultCode  activity result code
     * *
     * @param data        Uri of the selected image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage,
                    filePathColumn, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()

                // covert file to bytes to pass to ArcGISFeature
                var imageByte = ByteArray(0)
                try {
                    val imageFile = File(picturePath)
                    imageByte = FileUtils.readFileToByteArray(imageFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val attachmentName = application.getString(R.string.attachment) + "_" + System.currentTimeMillis() + ".png"

                progressDialog!!.setTitle(application.getString(R.string.apply_edit_message))
                progressDialog!!.setMessage(application.getString(R.string.wait))

                progressDialog!!.show()

                val addResult = mSelectedArcGISFeature!!.addAttachmentAsync(imageByte, "image/png", attachmentName)

                addResult.addDoneListener {
                    val tableResult = mServiceFeatureTable!!.updateFeatureAsync(mSelectedArcGISFeature!!)
                    tableResult.addDoneListener { applyServerEdits() }
                }
            }
        }


    }

    /**
     * Applies changes from a Service Feature Table to the server.
     */
    private fun applyServerEdits() {

        try {
            // check that the feature table was successfully updated
            // apply edits to the server
            val updatedServerResult = mServiceFeatureTable!!.applyEditsAsync()
            updatedServerResult.addDoneListener {
                try {
                    val edits = updatedServerResult.get()
                    if (edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            if (progressDialog!!.isShowing) {
                                progressDialog!!.dismiss()
                            }
                            //attachmentList.add(fileName);
                            mAttributeID = mSelectedArcGISFeature!!.attributes["objectid"].toString()
                            fetchAttachmentsFromServer(mAttributeID!!)
                            // update the attachment list view on the control panel
                            Toast.makeText(this@EditAttachmentActivity, application.getString(R.string.success_message), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@EditAttachmentActivity, application.getString(R.string.failure_message), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@EditAttachmentActivity, application.getString(R.string.failure_edit_results), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * send the updated attachment count back to EditFeatureAttachActivity and finish the current Activity
     */
    override fun onBackPressed() {

        val intent = Intent()
        intent.putExtra(application.getString(R.string.noOfAttachments), attachmentList.size)
        setResult(Activity.RESULT_OK, intent)
        finish()
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            permissionsGranted = true
            if (requestCode == requestCodeGallery) {
                selectAttachment()
            } else {
                fetchAttachmentAsync(listPosition, listView!!)
            }

        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(this@EditAttachmentActivity, resources.getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show()
            permissionsGranted = false

        }
    }
}
