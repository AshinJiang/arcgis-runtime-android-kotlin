package com.shaunsheep.agsdemo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.shaunsheep.agsdemo.addgraphicsrenderer.AddGraphicsRenderer
import com.shaunsheep.agsdemo.addgraphicssymbol.AddGraphicsSymbol
import com.shaunsheep.agsdemo.arcgismapimagelayer.MapImageLayerUrl
import com.shaunsheep.agsdemo.arcgistiledfromurl.TiledLayerFromUrl
import com.shaunsheep.agsdemo.arcgisvectortiledlayerurl.VectorTiledlayerFromUrl
import com.shaunsheep.agsdemo.blendrenderer.BlendRendererActivity
import com.shaunsheep.agsdemo.changebasemap.ChangeBaseMapActivity
import com.shaunsheep.agsdemo.changefeaturelayerrenderer.ChangeFeatureLayerRendererActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),OnItemClickListener {
    private val items= listOf(
            "add-graphics-renderer",
            "add-graphics-symbols",
            "arcgis-map-imagelayer-url",
            "arcgis-tiledlayer-url",
            "arcgis-vectortiledlayer-url",
            "authentication-profile",
            "blend-renderer",
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
            2 -> startActivity(Intent(applicationContext,MapImageLayerUrl::class.java))
            3 -> startActivity(Intent(applicationContext,TiledLayerFromUrl::class.java))
            4 -> startActivity(Intent(applicationContext,VectorTiledlayerFromUrl::class.java))

            6 -> startActivity(Intent(applicationContext,BlendRendererActivity::class.java))
            7 -> startActivity(Intent(applicationContext,ChangeBaseMapActivity::class.java))
            8 -> startActivity(Intent(applicationContext,ChangeFeatureLayerRendererActivity::class.java))

        }
        Snackbar.make(view,position.toString(),Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }
}
