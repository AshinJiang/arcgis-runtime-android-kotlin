package com.shaunsheep.agsdemo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.esri.arcgisruntime.mapping.view.MapRotationChangedEvent
import com.shaunsheep.agsdemo.addgraphicsrenderer.AddGraphicsRenderer
import com.shaunsheep.agsdemo.addgraphicssymbol.AddGraphicsSymbol
import com.shaunsheep.agsdemo.arcgismapimagelayer.MapImageLayerUrl
import com.shaunsheep.agsdemo.arcgistiledfromurl.TiledLayerFromUrl
import com.shaunsheep.agsdemo.arcgisvectortiledlayerurl.VectorTiledlayerFromUrl
import com.shaunsheep.agsdemo.blendrenderer.BlendRendererActivity
import com.shaunsheep.agsdemo.changebasemap.ChangeBaseMapActivity
import com.shaunsheep.agsdemo.changefeaturelayerrenderer.ChangeFeatureLayerRendererActivity
import com.shaunsheep.agsdemo.changesublayervisible.ChangeSublayerVisibleActivity
import com.shaunsheep.agsdemo.changeviewpoint.ChangeViewPointActivity
import com.shaunsheep.agsdemo.colormaprenderer.ColormapRendererActivity
import com.shaunsheep.agsdemo.creategeometry.CreateGeometriesActivity
import com.shaunsheep.agsdemo.createsavemap.CreateSaveMapActivity
import com.shaunsheep.agsdemo.displaydevicelocation.DisplayDeviceLocationActivity
import com.shaunsheep.agsdemo.displaydrawingstatus.DisplayDrawingStatusActivity
import com.shaunsheep.agsdemo.displaylayerviewstate.DisplayLayerViewStateActivity
import com.shaunsheep.agsdemo.displaymap.DisplayMapActivity
import com.shaunsheep.agsdemo.editfeatureattachments.EditFeatureAttachActivity
import com.shaunsheep.agsdemo.featurelayerdefinitionexpression.FeatureLayerDefExpressionActivity
import com.shaunsheep.agsdemo.featurelayerfeatureservice.FeatureLayerServiceActivity
import com.shaunsheep.agsdemo.featurelayergeodatabase.FeatureLayerGeodatabaseActivity
import com.shaunsheep.agsdemo.featurelayerquery.FeatureLayerQueryActivity
import com.shaunsheep.agsdemo.featurelayerselection.FeatureLayerSelectionActivity
import com.shaunsheep.agsdemo.featurelayershowattributes.FeatureLayerShowAttrActivity
import com.shaunsheep.agsdemo.featurelayerupdateattributes.FeatureLayerUpAttrActivity
import com.shaunsheep.agsdemo.featurelayerupdategeometry.FeatureLayerUpGeoActivity
import com.shaunsheep.agsdemo.findroute.FindRouteActivity
import com.shaunsheep.agsdemo.identifygraphicoverlay.IdentifyGraphicsOverlayActivity
import com.shaunsheep.agsdemo.managebookmarks.ManagerBookmarksActivity
import com.shaunsheep.agsdemo.manageoperationallayers.ManageOperationalLayerActivity
import com.shaunsheep.agsdemo.maploaded.MapLoadedActivity
import com.shaunsheep.agsdemo.maprotation.MapRotationActivity
import com.shaunsheep.agsdemo.mapsketching.MapSketchingActivity
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
            "colormap-renderer",
            "create-geometries",
            "create-save-map",
            "display-device-location",
            "display-drawing-status",
            "display-layer-view-state",
            "display-map",
            "edit-feature-attachments",
            "feature-layer-definition-expression",
            "feature-layer-feature-service",
            "feature-layer-geodatabase",
            "feature-layer-query",
            "feature-layer-selection",
            "feature-layer-show-attributes",
            "feature-layer-update-attributes",
            "feature-layer-update-geometry",
            "find-route",
            "identify-graphics-hittest",
            "manage-bookmarks",
            "manage-operational-layers",
            "map-loaded",
            "map-rotation",
            "map-sketching"
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
            9 -> startActivity(Intent(applicationContext,ChangeSublayerVisibleActivity::class.java))
            10 -> startActivity(Intent(applicationContext,ChangeViewPointActivity::class.java))
            11 -> startActivity(Intent(applicationContext,ColormapRendererActivity::class.java))
            12 -> startActivity(Intent(applicationContext,CreateGeometriesActivity::class.java))
            13 -> startActivity(Intent(applicationContext,CreateSaveMapActivity::class.java))
            14 -> startActivity(Intent(applicationContext,DisplayDeviceLocationActivity::class.java))
            15 -> startActivity(Intent(applicationContext,DisplayDrawingStatusActivity::class.java))
            16 -> startActivity(Intent(applicationContext,DisplayLayerViewStateActivity::class.java))
            17 -> startActivity(Intent(applicationContext,DisplayMapActivity::class.java))
            18 -> startActivity(Intent(applicationContext,EditFeatureAttachActivity::class.java))
            19 -> startActivity(Intent(applicationContext,FeatureLayerDefExpressionActivity::class.java))
            20 -> startActivity(Intent(applicationContext,FeatureLayerServiceActivity::class.java))
            21 -> startActivity(Intent(applicationContext,FeatureLayerGeodatabaseActivity::class.java))
            22 -> startActivity(Intent(applicationContext,FeatureLayerQueryActivity::class.java))
            23 -> startActivity(Intent(applicationContext,FeatureLayerSelectionActivity::class.java))
            24 -> startActivity(Intent(applicationContext,FeatureLayerShowAttrActivity::class.java))
            25 -> startActivity(Intent(applicationContext,FeatureLayerUpAttrActivity::class.java))
            26 -> startActivity(Intent(applicationContext,FeatureLayerUpGeoActivity::class.java))
            27 -> startActivity(Intent(applicationContext,FindRouteActivity::class.java))
            28 -> startActivity(Intent(applicationContext,IdentifyGraphicsOverlayActivity::class.java))
            29 -> startActivity(Intent(applicationContext,ManagerBookmarksActivity::class.java))
            30 -> startActivity(Intent(applicationContext,ManageOperationalLayerActivity::class.java))
            31 -> startActivity(Intent(applicationContext,MapLoadedActivity::class.java))
            32 -> startActivity(Intent(applicationContext,MapRotationActivity::class.java))
            33 -> startActivity(Intent(applicationContext,MapSketchingActivity::class.java))

        }
        Snackbar.make(view,position.toString(),Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }
}
