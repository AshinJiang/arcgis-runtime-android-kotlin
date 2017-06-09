package com.shaunsheep.agsdemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.shaunsheep.agsdemo.addgraphicsrenderer.AddGraphicsRenderer
import com.shaunsheep.agsdemo.addgraphicssymbol.AddGraphicsSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),OnItemClickListener {
    private val items= listOf(
            "add-graphics-renderer",
            "add-graphics-symbols",
            "arcgis-map-imagelayer-url",
            "arcgis-tiledlayer-url",
            "arcgis-vectortiledlayer-url",
            "authentication-profile",
            "change-basemaps",
            "change-feature-layer-renderer",
            "change-sublayer-visibility",
            "change-viewpoint",
            "create-geometries"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val linearLayout=LinearLayoutManager(this)
        linearLayout.orientation=LinearLayoutManager.VERTICAL
        main_recyclerView.layoutManager=linearLayout
        val demoAdapter=DemoAdaptor(items)
        main_recyclerView.adapter=demoAdapter
        demoAdapter.setOnItemClickListener(this)// 设置子项的点击事件
    }
    override fun onClick(view: View, position: Int) {
        when(position){
            0 -> startActivity(Intent(applicationContext,AddGraphicsRenderer::class.java))//跳转activity
            1 -> startActivity(Intent(applicationContext,AddGraphicsSymbol::class.java))

        }
        Snackbar.make(view,position.toString(),Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }
}
