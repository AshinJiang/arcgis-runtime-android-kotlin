package com.shaunsheep.agsdemo.addgraphicsrenderer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PolygonBuilder
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.*
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.activity_mapview.*

class AddGraphicsRenderer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapview)
        setTitle(R.string.title_add_graphics_renderer)

        val map= ArcGISMap(Basemap.Type.TOPOGRAPHIC,15.169193,16.333479,2)
        addGraphicsOverlay()
        mapView.map=map
    }
    fun addGraphicsOverlay(){
        // point graphic
        val pointGeometry=Point(40e5,40e5, SpatialReference.create(3857))
        val pointSymbol= SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND,
                Color.RED,10f)
        val pointGraphic=Graphic(pointGeometry)
        val pointGraphicOverlay=GraphicsOverlay()
        val pointRenderer=SimpleRenderer(pointSymbol)
        pointGraphicOverlay.renderer=pointRenderer as Renderer
        pointGraphicOverlay.graphics.add(pointGraphic)
        mapView.graphicsOverlays.add(pointGraphicOverlay)

        // line graphic
        val lineGeometry= PolylineBuilder(SpatialReference.create(3857))
        lineGeometry.addPoint(-10e5,40e5)
        lineGeometry.addPoint(20e5,50e5)
        // solid blue line symbol
        val lineSymbol=SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.BLUE,5f)
        // create graphic for polyline
        val lineGraphic=Graphic(lineGeometry.toGeometry())
        // create graphic overlay for polyline
        val lineGraphicOverlay=GraphicsOverlay()
        // create simple renderer
        val lineRenderer=SimpleRenderer(lineSymbol)
        // set renderer to overlay
        lineGraphicOverlay.renderer=lineRenderer
        // add graphic to overlay
        lineGraphicOverlay.graphics.add(lineGraphic)
        // add graphics overlay to the mapview
        mapView.graphicsOverlays.add(lineGraphicOverlay)

        // polygon graphic
        val polygonGeometry=PolygonBuilder(SpatialReference.create(3857))
        polygonGeometry.addPoint(-20e5, 20e5)
        polygonGeometry.addPoint(20e5, 20e5)
        polygonGeometry.addPoint(20e5, -20e5)
        polygonGeometry.addPoint(-20e5, -20e5)
        val polygonSymbol=SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,Color.YELLOW,null)
        val polygonGraphic=Graphic(polygonGeometry.toGeometry())
        val polygonGraphicOverlay=GraphicsOverlay()
        val polygonRenderer=SimpleRenderer(polygonSymbol)
        polygonGraphicOverlay.renderer=polygonRenderer
        polygonGraphicOverlay.graphics.add(polygonGraphic)
        mapView.graphicsOverlays.add(polygonGraphicOverlay)
    }
    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}
