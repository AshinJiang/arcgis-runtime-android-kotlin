package com.shaunsheep.agsdemo.createsavemap

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.security.OAuthLoginManager
import com.esri.arcgisruntime.security.OAuthTokenCredential
import com.shaunsheep.agsdemo.R
import java.util.*
import java.util.concurrent.ExecutionException


class MapSaveActivity : AppCompatActivity() {
    private var saveFab: FloatingActionButton? = null
    private var oauthLoginManager: OAuthLoginManager? = null
    private var oauthCred: OAuthTokenCredential? = null
    private var mTitleEditText: EditText? = null
    private var mTagsEditText: EditText? = null
    private var mDescEditText: EditText? = null
    private var mTagsList = ArrayList<String>()
    private var mDescription: String? = null
    private var mTitle: String? = null
    private var portal: Portal? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        // Get the OAuthLoginManager object from the main activity.
        oauthLoginManager = CreateSaveMapActivity.getOAuthLoginManagerInstance()
        if (oauthLoginManager == null) {
            return
        }
        fetchCredentials(intent)

        setContentView(R.layout.create_save_map_activity)

        // inflate the EditText boxes
        mTitleEditText = findViewById(R.id.titleText) as EditText
        mTagsEditText =  findViewById(R.id.tagText) as EditText
        mDescEditText =  findViewById(R.id.descText) as EditText
        progressDialog = ProgressDialog(this)
        saveFab =  findViewById(R.id.saveFab) as FloatingActionButton

        // add a click listener for Floating Action Button
        saveFab!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                // get Title, Tags and Description from the UI
                val flag = getMapAdditionalInfo()

                // if Title and tags are present
                if (flag) {
                    // inflate portal settings from array
                    val portalSettings = resources.getStringArray(R.array.portal)
                    // create a Portal using the portal url from the array
                    portal = Portal(portalSettings[1], true)
                    // set the credentials from the browser
                    portal!!.credential=oauthCred


                    progressDialog!!.setTitle(getString(R.string.author_map_message))
                    progressDialog!!.setMessage(getString(R.string.wait))
                    progressDialog!!.show()

                    portal!!.addDoneLoadingListener(Runnable {
                        // if portal is LOADED, save the map to the portal
                        if (portal!!.loadStatus === LoadStatus.LOADED) {
                            // Save the map to an authenticated Portal, with specified title, tags, description, and thumbnail.
                            // Passing 'null' as portal folder parameter saves this to users root folder.
                            val saveAsFuture = CreateSaveMapActivity.mMap!!.saveAsAsync(portal, null, mTitle, mTagsList, mDescription, null, true)
                            saveAsFuture.addDoneListener(Runnable {
                                // Check the result of the save operation.
                                try {
                                    if (progressDialog!!.isShowing) {
                                        progressDialog!!.dismiss()
                                    }
                                    val newMapPortalItem = saveAsFuture.get()
                                    Toast.makeText(applicationContext, getString(R.string.map_successful), Toast.LENGTH_SHORT).show()
                                } catch (e: InterruptedException) {
                                    // If saving failed, deal with failure depending on the cause...
                                    Log.e("Exception", e.toString())
                                } catch (e: ExecutionException) {
                                    Log.e("Exception", e.toString())
                                }
                            })
                        }
                    })
                    portal!!.loadAsync()
                }
            }
        })
    }

    /**
     * fetch the OAuth token from the OAuthLogin Manager

     * @param intent
     */
    private fun fetchCredentials(intent: Intent) {
        // Fetch oauth access token.
        val future = oauthLoginManager!!.fetchOAuthTokenCredentialAsync(intent)
        future.addDoneListener {
            try {
                oauthCred = future.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Get the Title, Tags and Description. Set error messages if title or tags are empty while saving a map

     * @return true - both Title and Tags fields are non empty else returns false
     */
    private fun getMapAdditionalInfo(): Boolean {
        mTitle = mTitleEditText!!.text.toString()
        mDescription = mDescEditText!!.text.toString()
        val tags = mTagsEditText!!.text.toString().split(",")
        for (tag in tags) {
            mTagsList.add(tag)
        }
//        Collections.addAll(mTagsList, tags)
        if (TextUtils.isEmpty(mTitle)) {
            mTitleEditText!!.error=(getString(R.string.title_error))
            return false
        }
        if (TextUtils.isEmpty(mTagsEditText!!.text.toString())) {
            mTagsEditText!!.error=(getString(R.string.tags_error))
            return false
        }

        return true
    }
}
