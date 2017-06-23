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

package com.shaunsheep.agsdemo.authenticationprofile

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutionException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalInfo
import com.esri.arcgisruntime.portal.PortalUser
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.shaunsheep.agsdemo.R

class AuthProfileActivity : AppCompatActivity() {

    private var userText: TextView? = null
    private var emailText: TextView? = null
    private var portalNameText: TextView? = null
    private var createDate: TextView? = null
    private var userImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_profile_activity)
        setTitle(R.string.title_auth_Profile)

        // Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
        val handler = DefaultAuthenticationChallengeHandler(this)
        AuthenticationManager.setAuthenticationChallengeHandler(handler)
        // Set loginRequired to true always prompt for credential,
        // When set to false to only login if required by the portal
        val portal = Portal("http://www.arcgis.com", true)
        portal.addDoneLoadingListener(Runnable {
            if (portal.loadStatus == LoadStatus.LOADED) {
                // Get the portal information
                val portalInformation = portal.portalInfo
                val portalName = portalInformation.portalName
                portalNameText = findViewById(R.id.portal) as TextView
                portalNameText!!.text = portalName

                // this portal does not require authentication, if null send toast message
                if (portal.user != null) {
                    // Get the authenticated portal user
                    val user = portal.user
                    // get the users full name
                    val userName = user.fullName
                    // update the textview
                    userText = findViewById(R.id.userName) as TextView
                    userText!!.text = userName
                    // get the users email
                    val email = user.email
                    // update the textview
                    emailText = findViewById(R.id.email) as TextView
                    emailText!!.text = email
                    // get the created date
                    val startDate = user.created
                    // format date
                    val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                    // get string format
                    val formatDate = simpleDateFormat.format(startDate.time)
                    // update textview
                    createDate = findViewById(R.id.create_date) as TextView
                    createDate!!.text = formatDate
                    // check if user profile thumbnail exists
                    if (user.thumbnailFileName == null) {
                        return@Runnable
                    }
                    // fetch the thumbnail
                    val thumbnailFuture = user.fetchThumbnailAsync()
                    thumbnailFuture.addDoneListener {
                        // get the thumbnail image data
                        val itemThumbnailData: ByteArray?
                        try {
                            itemThumbnailData = thumbnailFuture.get()

                            if (itemThumbnailData != null && itemThumbnailData.size > 0) {
                                // create a Bitmap to use as required
                                val itemThumbnail = BitmapFactory.decodeByteArray(itemThumbnailData, 0, itemThumbnailData.size)
                                // set the Bitmap onto the ImageView
                                userImage = findViewById(R.id.userImage) as ImageView
                                userImage!!.setImageBitmap(itemThumbnail)
                            }
                        } catch (e: InterruptedException) {
                            Log.d("TEST", e.message)
                        } catch (e: ExecutionException) {
                            Log.d("TEST", e.message)
                        }
                    }
                } else {
                    // send message that user did not authenticate
                    Toast.makeText(applicationContext, "User did not authenticate against " + portalName, Toast.LENGTH_LONG).show()
                }

            }
        })
        portal.loadAsync()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}

