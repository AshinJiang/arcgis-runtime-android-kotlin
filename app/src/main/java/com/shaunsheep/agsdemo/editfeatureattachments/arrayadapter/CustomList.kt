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

package com.shaunsheep.agsdemo.editfeatureattachments.arrayadapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.shaunsheep.agsdemo.R
import java.util.*

class CustomList(private val context: Activity,
                 private val attachmentName: ArrayList<String>) : ArrayAdapter<String>(context, R.layout.edit_feature_attachment_entry, attachmentName) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            val inflater = context.layoutInflater
            convertView = inflater.inflate(R.layout.edit_feature_attachment_entry, null, true)

            holder = ViewHolder()
            holder.textTitle = convertView!!.findViewById(R.id.AttachmentName) as TextView

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.textTitle!!.text = attachmentName[position]

        return convertView
    }

    private class ViewHolder {
        var textTitle: TextView? = null
    }
}
