package com.shaunsheep.agsdemo.addgraphicssymbol

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.shaunsheep.agsdemo.R
import kotlinx.android.synthetic.main.add_graphics_symbols.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol

class AddGraphicsSymbol : AppCompatActivity() {

    private val wgs84=SpatialReference.create(4326)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_graphics_symbols)
        setTitle(R.string.add_graphics_symbol)

        val map=ArcGISMap(Basemap.Type.OCEANS,56.075844, -2.681572, 11)
        mapView.map=map
        val graphicsOverlay=GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

        addBuoyPoints(graphicsOverlay)
        //add boat trip polyline to graphics overlay
        addBoatTrip(graphicsOverlay)
        //add nesting ground polygon to graphics overlay
        addNestingGround(graphicsOverlay)
        //add text symbols and points to graphics overlay
        addText(graphicsOverlay)
    }

    private fun addBuoyPoints(graphicOverlay: GraphicsOverlay) {
        //define the buoy locations
        val buoy1Loc = Point(-2.712642647560347, 56.062812566811544, wgs84)
        val buoy2Loc = Point(-2.6908416959572303, 56.06444173689877, wgs84)
        val buoy3Loc = Point(-2.6697273884990937, 56.064250073402874, wgs84)
        val buoy4Loc = Point(-2.6395150461199726, 56.06127916736989, wgs84)
        //create a marker symbol
        val buoyMarker = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)
        //create graphics
        val buoyGraphic1 = Graphic(buoy1Loc, buoyMarker)
        val buoyGraphic2 = Graphic(buoy2Loc, buoyMarker)
        val buoyGraphic3 = Graphic(buoy3Loc, buoyMarker)
        val buoyGraphic4 = Graphic(buoy4Loc, buoyMarker)
        //add the graphics to the graphics overlay
        graphicOverlay.graphics.add(buoyGraphic1)
        graphicOverlay.graphics.add(buoyGraphic2)
        graphicOverlay.graphics.add(buoyGraphic3)
        graphicOverlay.graphics.add(buoyGraphic4)
    }

    private fun addText(graphicOverlay: GraphicsOverlay) {
        //create a point geometry
        val bassLocation = Point(-2.640631, 56.078083, wgs84)
        val craigleithLocation = Point(-2.720324, 56.073569, wgs84)

        //create text symbols
        val bassRockSymbol = TextSymbol(10f, "Bass Rock", Color.rgb(0, 0, 230),
                TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM)
        val craigleithSymbol = TextSymbol(10f, "Craigleith", Color.rgb(0, 0, 230),
                TextSymbol.HorizontalAlignment.RIGHT, TextSymbol.VerticalAlignment.TOP)

        //define a graphic from the geometry and symbol
        val bassRockGraphic = Graphic(bassLocation, bassRockSymbol)
        val craigleithGraphic = Graphic(craigleithLocation, craigleithSymbol)
        //add the text to the graphics overlay
        graphicOverlay.graphics.add(bassRockGraphic)
        graphicOverlay.graphics.add(craigleithGraphic)
    }

    private fun addBoatTrip(graphicOverlay: GraphicsOverlay) {
        //define a polyline for the boat trip
        val boatRoute = getBoatTripGeometry()
        //define a line symbol
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(128, 0, 128), 4f)
        //create the graphic
        val boatTripGraphic = Graphic(boatRoute, lineSymbol)
        //add to the graphic overlay
        graphicOverlay.graphics.add(boatTripGraphic)
    }

    private fun addNestingGround(graphicOverlay: GraphicsOverlay) {
        //define the polygon for the nesting ground
        val nestingGround = getNestingGroundGeometry()
        //define the fill symbol and outline
        val outlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(0, 0, 128), 1f)
        val fillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.rgb(0, 80, 0), outlineSymbol)
        //define graphic
        val nestingGraphic = Graphic(nestingGround, fillSymbol)
        //add to graphics overlay
        graphicOverlay.graphics.add(nestingGraphic)
    }

    private fun getBoatTripGeometry(): Polyline {
        //a new point collection to make up the polyline
        val boatPositions = PointCollection(wgs84)
        //add positions to the point collection
        boatPositions.add(Point(-2.7184791227926772, 56.06147084563517))
        boatPositions.add(Point(-2.7196807500463924, 56.06147084563517))
        boatPositions.add(Point(-2.722084004553823, 56.062141712059706))
        boatPositions.add(Point(-2.726375530459948, 56.06386674355254))
        boatPositions.add(Point(-2.726890513568683, 56.0660708381432))
        boatPositions.add(Point(-2.7270621746049275, 56.06779569383808))
        boatPositions.add(Point(-2.7255172252787228, 56.068753913653914))
        boatPositions.add(Point(-2.723113970771293, 56.069424653352335))
        boatPositions.add(Point(-2.719165766937657, 56.07028701581465))
        boatPositions.add(Point(-2.713672613777817, 56.070574465681325))
        boatPositions.add(Point(-2.7093810878716917, 56.07095772883556))
        boatPositions.add(Point(-2.7044029178205866, 56.07153261642126))
        boatPositions.add(Point(-2.698223120515766, 56.072394931722265))
        boatPositions.add(Point(-2.6923866452834355, 56.07325722773041))
        boatPositions.add(Point(-2.68672183108735, 56.07335303720707))
        boatPositions.add(Point(-2.6812286779275096, 56.07354465544585))
        boatPositions.add(Point(-2.6764221689126497, 56.074215311778964))
        boatPositions.add(Point(-2.6698990495353394, 56.07488595644139))
        boatPositions.add(Point(-2.6647492184479886, 56.075748196715914))
        boatPositions.add(Point(-2.659427726324393, 56.076131408423215))
        boatPositions.add(Point(-2.654792878345778, 56.07622721075461))
        boatPositions.add(Point(-2.651359657620878, 56.076514616319784))
        boatPositions.add(Point(-2.6477547758597324, 56.07708942101955))
        boatPositions.add(Point(-2.6450081992798125, 56.07814320736718))
        boatPositions.add(Point(-2.6432915889173625, 56.08025069360931))
        boatPositions.add(Point(-2.638656740938747, 56.08044227755186))
        boatPositions.add(Point(-2.636940130576297, 56.078813783674946))
        boatPositions.add(Point(-2.636425147467562, 56.07728102068079))
        boatPositions.add(Point(-2.637798435757522, 56.076610417698504))
        boatPositions.add(Point(-2.638656740938747, 56.07507756705851))
        boatPositions.add(Point(-2.641231656482422, 56.07479015077557))
        boatPositions.add(Point(-2.6427766058086277, 56.075748196715914))
        boatPositions.add(Point(-2.6456948434247924, 56.07546078543464))
        boatPositions.add(Point(-2.647239792750997, 56.074598538729404))
        boatPositions.add(Point(-2.6492997251859376, 56.072682365868616))
        boatPositions.add(Point(-2.6530762679833284, 56.0718200569986))
        boatPositions.add(Point(-2.655479522490758, 56.070861913404286))
        boatPositions.add(Point(-2.6587410821794135, 56.07047864929729))
        boatPositions.add(Point(-2.6633759301580286, 56.07028701581465))
        boatPositions.add(Point(-2.666637489846684, 56.07009538137926))
        boatPositions.add(Point(-2.670070710571584, 56.06990374599109))
        boatPositions.add(Point(-2.6741905754414645, 56.069137194910745))
        boatPositions.add(Point(-2.678310440311345, 56.06808316228391))
        boatPositions.add(Point(-2.682086983108735, 56.06789151689155))
        boatPositions.add(Point(-2.6868934921235956, 56.06760404701653))
        boatPositions.add(Point(-2.6911850180297208, 56.06722075051504))
        boatPositions.add(Point(-2.695133221863356, 56.06702910083509))
        boatPositions.add(Point(-2.698223120515766, 56.066837450202335))
        boatPositions.add(Point(-2.7016563412406667, 56.06645414607839))
        boatPositions.add(Point(-2.7061195281830366, 56.0660708381432))
        boatPositions.add(Point(-2.7100677320166717, 56.065591697864576))
        boatPositions.add(Point(-2.713329291705327, 56.06520838135397))
        boatPositions.add(Point(-2.7167625124302273, 56.06453756828941))
        boatPositions.add(Point(-2.718307461756433, 56.06348340989081))
        boatPositions.add(Point(-2.719165766937657, 56.062812566811544))
        boatPositions.add(Point(-2.7198524110826376, 56.06204587471371))
        boatPositions.add(Point(-2.719165766937657, 56.06166252294756))
        boatPositions.add(Point(-2.718307461756433, 56.06147084563517))

        //create the polyline from the point collection
        return Polyline(boatPositions)
    }

    private fun getNestingGroundGeometry(): Polygon {

        //a new point collection to make up the polygon
        val points = PointCollection(wgs84)

        //add points to the point collection
        points.add(Point(-2.643077012566659, 56.077125346044475))
        points.add(Point(-2.6428195210159444, 56.07717324600376))
        points.add(Point(-2.6425405718360033, 56.07774804087097))
        points.add(Point(-2.6427122328698127, 56.077927662508635))
        points.add(Point(-2.642454741319098, 56.07829887790651))
        points.add(Point(-2.641853927700763, 56.078526395253725))
        points.add(Point(-2.6409741649024867, 56.078801809192434))
        points.add(Point(-2.6399871139580795, 56.07881378366685))
        points.add(Point(-2.6394077579689705, 56.07908919555142))
        points.add(Point(-2.638764029092183, 56.07917301616904))
        points.add(Point(-2.638485079912242, 56.07896945149566))
        points.add(Point(-2.638570910429147, 56.078203080726844))
        points.add(Point(-2.63878548672141, 56.077568418396))
        points.add(Point(-2.6391931816767085, 56.077197195961084))
        points.add(Point(-2.6399441986996273, 56.07675411934114))
        points.add(Point(-2.6406523004640934, 56.076730169108444))
        points.add(Point(-2.6406737580933193, 56.07632301287509))
        points.add(Point(-2.6401802326211157, 56.075999679860494))
        points.add(Point(-2.6402446055087943, 56.075844000034046))
        points.add(Point(-2.640416266542604, 56.07578412301025))
        points.add(Point(-2.6408883343855822, 56.075808073830935))
        points.add(Point(-2.6417680971838577, 56.076239186057734))
        points.add(Point(-2.642197249768383, 56.076251161328514))
        points.add(Point(-2.6428409786451708, 56.07661041772168))
        points.add(Point(-2.643077012566659, 56.077125346044475))

        //create a polygon from the point collection
        return Polygon(points)
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
        if (keyCode==KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}
