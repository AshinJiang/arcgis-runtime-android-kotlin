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

package com.shaunsheep.agsdemo.featurelayerupdateattributes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import com.shaunsheep.agsdemo.R

/**
 * Displays the Damage type options in a ListView.
 */
class FLUPAttrDamageTypesListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feature_layer_u_attr_damage_types_listview)
        setTitle(R.string.select_damage_Type)
        val damageTypes = resources.getStringArray(R.array.damage_types)

        val listView = findViewById(R.id.listview) as ListView

        listView.adapter = ArrayAdapter(this, R.layout.feature_layer_u_attr_damage_types, damageTypes)

        listView.isTextFilterEnabled = true

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val myIntent = Intent()
            myIntent.putExtra("typdamage", damageTypes[position]) //Optional parameters
            setResult(100, myIntent)
            finish()
        }

    }

    override fun onBackPressed() {}
}
