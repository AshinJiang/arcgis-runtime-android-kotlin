/* Copyright 2015 Esri
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

package com.shaunsheep.agsdemo.mapsketching

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol

import java.util.ArrayList
import java.util.Stack
import java.util.concurrent.ExecutionException

/**
 * Wraps a GraphicsOverlay with additional logic for sketching points, lines, and polygons onto
 * the GraphicsOverlay. Also supports undoing/redoing of sketching actions, as well as clearing
 * entire GraphicsOverlay of all current sketches.
 */
class SketchGraphicsOverlay
/**
 * Instantiates a SketchGraphicsOverlay with the specified MapView (to which the overlay is
 * added) and SketchGraphicsOverlayEventListener (used to notify the main activity when undo/
 * redo/clear buttons should be enabled/disabled and when a drawing is finished.

 * @param mapView the MapView to which the overlay should be added
 * *
 * @param listener the listener to notify upon state changes
 */
(private val mMapView: MapView, // Listener is used to notify when undo/redo/clear buttons can be enabled and
        // when a drawing is finished
 private val mListener: SketchGraphicsOverlayEventListener) {
    private val mGraphicsOverlay: GraphicsOverlay
    private val mGraphics: MutableList<Graphic>
    // Symbols used when drawing new points/lines/polygons
    private val mPointPlacementSymbol: SimpleMarkerSymbol
    private val mPointPlacedSymbol: SimpleMarkerSymbol
    private val mPolylineVertexSymbol: SimpleMarkerSymbol
    private val mPolylinePlacementSymbol: SimpleLineSymbol
    private val mPolylinePlacedSymbol: SimpleLineSymbol
    private val mPolylineMidpointSymbol: SimpleMarkerSymbol
    private val mPolygonFillSymbol: SimpleFillSymbol
    // Keep a reference to the current point, line, and/or polygon because drawn
    private var mCurrentPoint: Graphic? = null
    private var mCurrentLine: Graphic? = null
    private var mCurrentPolygon: Graphic? = null
    // Keep a reference to the current point collection for polyline/polygon to update geometry
    private var mCurrentPointCollection: PointCollection? = null
    // Current drawing mode
    private var mDrawingMode = DrawingMode.NONE
    // The first point of a polyline uses special logic so keep track of when it's started
    private var mIsPolylineStarted = false
    private var mIsMidpointSelected = false
    // Stack of actions to be undone
    private val mUndoElementStack = Stack<UndoRedoItem>()
    // stack of actions to be redone
    private val mRedoElementStack = Stack<UndoRedoItem>()

    init {
        mGraphicsOverlay = GraphicsOverlay()
        // Add a graphics overlay and get our list of graphics for modification
        mMapView.graphicsOverlays.add(mGraphicsOverlay)
        mGraphics = mGraphicsOverlay.graphics
        // Set a drawing touch listener for sketching
        mMapView.setOnTouchListener(DrawingMapViewOnTouchListener(mMapView.context, mMapView))

        // Outline symbols for outlining the main symbols
        val blackOutline = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 0), 1f)
        val whiteOutline = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(255, 255, 255), 1f)

        // Create all the different symbols
        // When placing a point, it will be a red circle with black outline
        mPointPlacementSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 7f)
        mPointPlacementSymbol.outline = blackOutline
        // A placed point (single point) will be a blue circle with black outline
        mPointPlacedSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 7f)
        mPointPlacedSymbol.outline = blackOutline
        // A placed vertex of a polyline will be a blue square with a white outline
        mPolylineVertexSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.BLUE, 5f)
        mPolylineVertexSymbol.outline = whiteOutline
        // While placing a polyline, the line will be red
        mPolylinePlacementSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1f)
        // Once placed, a polyline will become blue
        mPolylinePlacedSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1f)
        // A midpoint of a polyline segment will be a semi-transparent white circle with black outline
        mPolylineMidpointSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.WHITE, 5f)
        mPolylineMidpointSymbol.outline = blackOutline
        // Polygons will be filled with a semi-transparent black solid shade
        mPolygonFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLACK, null)
    }

    /**
     * Sets the current drawing mode of the SketchGraphicsOverlay.

     * @param drawingMode the drawing mode to set
     */
    fun setDrawingMode(drawingMode: DrawingMode) {
        // If we try to start a new drawing before finishing our last, finish the current one
        if (mDrawingMode != DrawingMode.NONE) {
            finishDrawing()
        }
        mDrawingMode = drawingMode
        // If the drawing mode is polyline or polygon, set the current point collection to an empty collection
        if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
            mCurrentPointCollection = PointCollection(mMapView.spatialReference)
        }
    }

    /**
     * Convenience method for queueing an undo or a redo event. In addition to queueing the
     * event, it will also notify the listener to enable the undo or redo button if the stack
     * was previously empty.

     * @param stack the stack to which the event should be added
     * *
     * @param item the UndoRedoItem to queue
     */
    private fun queueUndoRedoItem(stack: Stack<UndoRedoItem>, item: UndoRedoItem) {
        // If the stack is currently empty, we should notify the listener to enable to button
        if (stack.isEmpty()) {
            // If it's the undo stack, fire the undo state changed listener
            if (stack === mUndoElementStack) {
                mListener.onUndoStateChanged(true)
                // Otherwise fire the redo state changed listener
            } else {
                mListener.onRedoStateChanged(true)
            }
        }
        // Finally, push the item to the stack
        stack.push(item)
    }

    /**
     * Undo the last event that took place.
     */
    fun undo() {
        // Handle an undo event, popping an event from the undo stack and pushing a new event to the redo stack
        handleUndoRedoEvent(mUndoElementStack, mRedoElementStack)
    }

    /**
     * Redo the action previously undone with a call to undo().
     */
    fun redo() {
        // Handle an redo event, popping an event from the redo stack and pushing a new event to the undo stack
        handleUndoRedoEvent(mRedoElementStack, mUndoElementStack)
    }

    /**
     * Convenience method for clearing the undo or redo event stack. Additionally notifies
     * the listener to disable the corresponding button.

     * @param stack the stack to clear
     */
    private fun clearStack(stack: Stack<UndoRedoItem>) {
        stack.clear()
        // Notify the listener based on which stack was cleared
        if (stack === mUndoElementStack) {
            mListener.onUndoStateChanged(false)
        } else {
            mListener.onRedoStateChanged(false)
        }
    }

    /**
     * This method handles performing an undo or redo event. An event will be popped from the specified
     * stack and an opposite event type (to undo/redo that) will be pushed into the other stack.

     * @param from the stack from which to pop an event
     * *
     * @param to   the stack in which to push the opposing event
     */
    private fun handleUndoRedoEvent(from: Stack<UndoRedoItem>, to: Stack<UndoRedoItem>) {
        // index is used in a couple places so define it here
        var index: Int
        val pointIndex: Int
        val graphics: List<Graphic>
        if (!from.isEmpty()) {
            val item = from.pop()
            // If this was the last event in the stock, notify the listener to disable the corresponding button
            if (from.isEmpty()) {
                if (from === mUndoElementStack) {
                    // disable to selected drawing mode
                    mListener.onDrawingFinished()
                    mListener.onUndoStateChanged(false)
                } else {
                    mListener.onRedoStateChanged(false)
                }
            }
            // Check whether the graphics list was empty before we process the event
            val graphicsWasEmpty = mGraphics.isEmpty()
            when (item.event) {
            // If the event was adding a graphic, then the action taken here is to remove the graphic
                SketchGraphicsOverlay.UndoRedoItem.Event.ADD_POINT -> {
                    // Get the graphic[s] previously added and remove them from the graphics list
                    graphics = item.element as List<Graphic>
                    mGraphics.removeAll(graphics)
                    // Queue a new event indicating that we've removed the graphic[s]
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.REMOVE_POINT, graphics))
                    mIsMidpointSelected = false
                    mIsPolylineStarted = false
                    mCurrentPoint = null
                    mCurrentPointCollection = PointCollection(mMapView.spatialReference)
                }
            // If the event was removing a graphic, then the action taken here is to add it back
                SketchGraphicsOverlay.UndoRedoItem.Event.REMOVE_POINT -> {
                    // Readd the graphic[s] previously removed.
                    graphics = item.element as List<Graphic>
                    mGraphics.addAll(graphics)
                    // Queue a new event indicating that we've added the graphic[s]
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics))
                }
            // If the event was adding a polyline point, the action taken here is to remove the last point added
                SketchGraphicsOverlay.UndoRedoItem.Event.ADD_POLYLINE_POINT -> {
                    // Get the index of the current point (which will be the one most recently added)
                    pointIndex = if (mDrawingMode == DrawingMode.POLYGON)
                        mCurrentPointCollection!!.size - 2
                    else
                        mCurrentPointCollection!!.size - 1
                    // Remove it from the point collection and update the current line (and polygon if applicable)
                    val p = mCurrentPointCollection!!.removeAt(pointIndex)
                    mCurrentLine!!.geometry = Polyline(mCurrentPointCollection)
                    if (mDrawingMode == DrawingMode.POLYGON) {
                        mCurrentPolygon!!.geometry = Polygon(mCurrentPointCollection)
                    }
                    // Undoing an add point always removes the final point
                    index = mGraphics.size - 1
                    // Remove the point, and remove the midpoint before it
                    mGraphics.removeAt(index--)
                    mGraphics.removeAt(index--)
                    // If we're drawing a polygon, we also need to update the final midpoint position
                    if (mDrawingMode == DrawingMode.POLYGON) {
                        updatePolygonMidpoint()
                        // If we are down to only 1 point (size will be 2 because 1st and final point are duplicates)
                        // Then we want to remove the final midpoint
                        if (mCurrentPointCollection!!.size == 2) {
                            mGraphics.removeAt(index--)
                            mCurrentPoint = mGraphics[index]
                        } else {
                            // Otherwise just set the point before the final midpoint as current point
                            mCurrentPoint = mGraphics[index - 1]
                        }
                    } else {
                        // If we're drawing a polyline then the current point will be the final point (which is where
                        // index will now be pointing)
                        mCurrentPoint = mGraphics[index]
                    }
                    // Change the symbol to the placement symbol
                    mCurrentPoint!!.symbol = mPointPlacementSymbol
                    // Queue a new event indicating that we've removed a polyline point
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.REMOVE_POLYLINE_POINT, p))
                }
            // If the event was moving a polyline point, the action taken here is to move it back
                SketchGraphicsOverlay.UndoRedoItem.Event.MOVE_POLYLINE_POINT -> {
                    // Get the corresponding MovePolylinePointElement
                    val element = item.element as UndoRedoItem.MovePolylinePointElement
                    // Queue a new event indicating a polyline point move with the necessary information
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.MOVE_POLYLINE_POINT,
                            UndoRedoItem.MovePolylinePointElement(mCurrentPoint!!, mCurrentPoint!!.geometry as Point, element.isMidpoint)))
                    // Get the old Graphic of the point that was moved
                    val oldGraphic = element.graphic
                    // Get the previous point position
                    val oldPoint = element.point
                    // Find the index of the moved point. Since we have complete control over how we're adding the undo elements,
                    // we can safely assume here that oldGraphic.getGeometry() is a Point. However, proper practice (here and other
                    // places) would be to check that the geometry is an instanceof Point before casting.
                    pointIndex = mCurrentPointCollection!!.indexOf(oldGraphic.geometry)
                    // Find the index of the moved graphic
                    index = mGraphics.indexOf(oldGraphic)
                    // Set the current working point's symbol to a placed vertex symbol before switching
                    mCurrentPoint!!.symbol = mPolylineVertexSymbol
                    // Change our current working point to the old moved graphic
                    mCurrentPoint = mGraphics[index]
                    // Set it's symbol to the placement symbol
                    mCurrentPoint!!.symbol = mPointPlacementSymbol
                    // If the element is/was a midpoint, we need to handle adding/removing surrounding midpoints
                    if (element.isMidpoint) {
                        var newGeometry = oldPoint
                        // If this is an undo
                        if (from === mUndoElementStack) {
                            // Go back to having a midpoint selected
                            mIsMidpointSelected = true
                            // Remove the current point from the point collection (since it's going back to being a midpoint)
                            mCurrentPointCollection!!.removeAt(pointIndex)
                            // Remove the midpoint before this point. Since this shifts the index, the index will now be
                            // for the midpoint after our point
                            mGraphics.removeAt(index - 1)
                            // So remove that index and then decrement to get the index back at our graphic
                            mGraphics.removeAt(index--)
                            // Our point will now be a midpoint so get the midpoint between the points before and after it and set it
                            val endPoint = if (mDrawingMode == DrawingMode.POLYGON && index == mGraphics.size - 1)
                                mCurrentPointCollection!![mCurrentPointCollection!!.size - 1]
                            else
                                mGraphics[index + 1].geometry as Point
                            newGeometry = getMidpoint(mGraphics[index - 1].geometry as Point, endPoint)
                        } else {
                            // If it's a redo, then we need to make a new vertex point and add new midpoints before and after it
                            splitMidpoint(newGeometry)
                        }
                        // Finally set the current point's position
                        mCurrentPoint!!.geometry = newGeometry
                    } else {
                        // If it wasn't a midpoint, then change the point's position within the point collection and update the
                        // graphic's geometry
                        mCurrentPointCollection!![pointIndex] = oldPoint
                        mCurrentPoint!!.geometry = oldPoint
                        // If this isn't the first point, adjust the midpoint's position before it
                        if (pointIndex != 0) {
                            val preMidpoint = getMidpoint(mCurrentPointCollection!![pointIndex - 1], oldPoint)
                            mGraphics[index - 1].geometry = preMidpoint
                        }
                        // If this isn't the last point, adjust the midpoints position after it
                        if (pointIndex != mCurrentPointCollection!!.size - 1) {
                            val postMidpoint = getMidpoint(oldPoint, mCurrentPointCollection!![pointIndex + 1])
                            mGraphics[index + 1].geometry = postMidpoint
                        }
                    }
                    if (mDrawingMode == DrawingMode.POLYGON) {
                        // If we're moving the first point of a polygon, we need to replicate that change
                        // in the final point as well and update the final midpoint
                        if (pointIndex == 0) {
                            mCurrentPointCollection!![mCurrentPointCollection!!.size - 1] = oldPoint
                            updatePolygonMidpoint()
                        }
                        // In either case, update the polygon's geometry
                        mCurrentPolygon!!.geometry = Polygon(mCurrentPointCollection)
                    }
                    // Update the line's geometry
                    mCurrentLine!!.geometry = Polyline(mCurrentPointCollection)
                }
            // If the event was removing a polyline point, the action taken here is to add it back
                SketchGraphicsOverlay.UndoRedoItem.Event.REMOVE_POLYLINE_POINT -> {
                    // Get the point that was removed, and add it back to the point collection
                    val point = item.element as Point
                    if (mDrawingMode == DrawingMode.POLYGON) {
                        // If adding back to a polygon, remove the final midpoint so it can be readded
                        if (mCurrentPointCollection!!.size > 2) {
                            mGraphics.removeAt(mGraphics.size - 1)
                        }
                        // Add it at the second to last position
                        mCurrentPointCollection!!.add(mCurrentPointCollection!!.size - 1, point)
                    } else {
                        // If just a line, add it in the final position
                        mCurrentPointCollection!!.add(point)
                    }
                    addPolylinePoint(point)
                    // Queue a new event indicating that we've added a polyline point
                    to.add(UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE_POINT,""))
                }
            // If the event was finishing a polyline, the action taken here is to remove the whole polyline
                SketchGraphicsOverlay.UndoRedoItem.Event.ADD_POLYLINE -> {
                    // Create a new graphics list and add to it all the pieces of the polyline, so we can add it back with a redo
                    graphics = ArrayList<Graphic>()
                    index = mGraphics.size - 1
                    // Add all of the points of the polyline
                    while (index > 0 && mGraphics[index].geometry !is Polyline) {
                        graphics.add(0, mGraphics.removeAt(index--))
                    }
                    // Add the polyline itself
                    graphics.add(0, mGraphics.removeAt(index--))
                    // If removing a polygon, also add the polygon
                    if (index > -1 && mGraphics[index].geometry is Polygon) {
                        graphics.add(0, mGraphics.removeAt(index))
                    }
                    // Queue a new event indicating that we've removed a polyline
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.REMOVE_POLYLINE, graphics))
                }
            // If the event was removing a polyline, the action taken here is to add it back
                SketchGraphicsOverlay.UndoRedoItem.Event.REMOVE_POLYLINE -> {
                    // Get the graphics that were previously removed
                    graphics = item.element as List<Graphic>
                    // Add them all to the list of graphics
                    mGraphics.addAll(graphics)
                    // Queue a new event indicating that we've added a polyline
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE, ""))
                }
            // If the event was moving a point, the action taken here is to move it back
                SketchGraphicsOverlay.UndoRedoItem.Event.MOVE_POINT -> if (mCurrentPoint != null) {
                    // Queue a new event indicating that we moved the point, with its current geometry before we change it
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, mCurrentPoint!!.geometry))
                    // Set the geometry back
                    mCurrentPoint!!.geometry = item.element as Geometry
                }
            // If the event was erasing all graphics, the action taken here is to put them all back
                SketchGraphicsOverlay.UndoRedoItem.Event.ERASE_GRAPHICS -> {
                    // Add all the graphics back
                    mGraphics.addAll(item.element as List<Graphic>)
                    // Queue a new event indicating that we've replaced all the graphics
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.REPLACE_GRAPHICS, ""))
                }
            // If the event was replacing all the graphics, the action taken here is to clear them all
                SketchGraphicsOverlay.UndoRedoItem.Event.REPLACE_GRAPHICS -> {
                    // Queue a new event indicating that we've erased the graphics
                    queueUndoRedoItem(to, UndoRedoItem(UndoRedoItem.Event.ERASE_GRAPHICS, copyGraphics()))
                    // Erase all graphics
                    mGraphics.clear()
                }
            }
            val graphicsIsEmpty = mGraphics.isEmpty()
            // If the graphic list was previously empty and now it's not, notify the listener to enable
            // the clear button
            if (graphicsWasEmpty && !graphicsIsEmpty) {
                mListener.onClearStateChanged(true)
                // If previously non empty and now it is, notify the listener to disable the clear button
            } else if (!graphicsWasEmpty && graphicsIsEmpty) {
                mListener.onDrawingFinished()
                mListener.onClearStateChanged(false)
            }
        }
    }

    /**
     * Clear all of the graphics on the SketchGraphicsOverlay and reset the current drawing state.
     */
    fun clear() {
        // Before clearing, finish any drawing that may currently be in progress
        finishDrawing()
        if (!mGraphics.isEmpty()) {
            queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.ERASE_GRAPHICS, copyGraphics()))
            mGraphics.clear()
        }
        mDrawingMode = DrawingMode.NONE
        mIsPolylineStarted = false
        mCurrentPoint = null
        mCurrentLine = null
        mCurrentPolygon = null
        mCurrentPointCollection = null
        mListener.onClearStateChanged(false)
    }

    /**
     * Creates a copy of the current graphics in the SketchGraphicsOverlay. This is used to replace graphics
     * after they have been cleared.

     * @return a copy of the current graphics list
     */
    private fun copyGraphics(): List<Graphic> {
        val graphicsCopy = ArrayList<Graphic>()
        for (i in mGraphics.indices) {
            graphicsCopy.add(mGraphics[i])
        }
        return graphicsCopy
    }

    /**
     * Helper method to get the midpoint of two points

     * @param a the first point
     * *
     * @param b the second point
     * *
     * @return the midpoint of the two points
     */
    private fun getMidpoint(a: Point, b: Point): Point {
        val midX = (a.x + b.x) / 2.0
        val midY = (a.y + b.y) / 2.0
        return Point(midX, midY, mMapView.spatialReference)
    }

    /**
     * Splits a line segment on the midpoint, adding a new vertex where the midpoint
     * had been and adding new midpoints before and after the new vertex.

     * @param newGeometry the position of the new vertex
     */
    private fun splitMidpoint(newGeometry: Point) {
        // get the index of the current working graphic
        var graphicIndex = mGraphics.indexOf(mCurrentPoint)
        val pointIndex: Int
        // If we're drawing a polygon and splitting the final midpoint then the index in which
        // to insert the new point will be second to last
        if (mDrawingMode == DrawingMode.POLYGON && graphicIndex == mGraphics.size - 1) {
            pointIndex = mCurrentPointCollection!!.size - 1
        } else {
            // If it's not a polygon or not the final midpoint, get the index in the point collection of
            // the point following the midpoint so the new vertex can be added before it
            val pointAfterMidpoint = mGraphics[graphicIndex + 1].geometry as Point
            // Since the midpoints aren't in the point collection, get the index of the point after it
            pointIndex = mCurrentPointCollection!!.indexOf(pointAfterMidpoint)
        }
        // Add a new point at this index with the midpoint's new geometry
        mCurrentPointCollection!!.add(pointIndex, newGeometry)
        // Find the locations of the new midpoints (before and after the just added vertex point)
        val newPreMidpoint = getMidpoint(mCurrentPointCollection!![pointIndex - 1], newGeometry)
        val newPostMidpoint = getMidpoint(newGeometry, mCurrentPointCollection!![pointIndex + 1])
        // The graphic index is current pointing at the old midpoint, so add the pre-midpoint here
        // which will shift the index. Increment the counter so it points at the old midpoint again
        mGraphics.add(graphicIndex++, Graphic(newPreMidpoint, mPolylineMidpointSymbol))
        // Add the post-midpoint at the index after the old midpoint
        mGraphics.add(graphicIndex + 1, Graphic(newPostMidpoint, mPolylineMidpointSymbol))
        // Now that we've split and added a new vertex, the selected point is no longer a midpoint
        mIsMidpointSelected = false
    }

    /**
     * Helper method to add a point to the polyline/polygon. Handles the work of
     * changing the working points symbol and updating the polyline/polygon geometry.

     * @param point the point to add
     */
    private fun addPolylinePoint(point: Point) {
        val midPoint = getMidpoint(mCurrentPoint!!.geometry as Point, point)
        mCurrentPoint!!.symbol = mPolylineVertexSymbol
        mCurrentLine!!.geometry = Polyline(mCurrentPointCollection)
        mGraphics.add(Graphic(midPoint, mPolylineMidpointSymbol))
        mCurrentPoint = Graphic(point, mPointPlacementSymbol)
        mGraphics.add(mCurrentPoint!!)
        if (mDrawingMode == DrawingMode.POLYGON) {
            mCurrentPolygon!!.geometry = Polygon(mCurrentPointCollection)
            val polygonMidpoint = getMidpoint(mCurrentPoint!!.geometry as Point, mCurrentPointCollection!![0])
            mGraphics.add(Graphic(polygonMidpoint, mPolylineMidpointSymbol))
        }
    }

    /**
     * Helper method to update the final midpoint of a polygon.
     */
    private fun updatePolygonMidpoint() {
        // There will only be a final midpoint if there are at least 3 points
        if (mCurrentPointCollection!!.size > 2) {
            // Get the final midpoint graphic and update its geometry with the midpoint of the final and first points
            val postMidpoint = mGraphics[mGraphics.size - 1]
            val postMidpointGeometry = getMidpoint(mCurrentPointCollection!![mCurrentPointCollection!!.size - 2], mCurrentPointCollection!![0])
            postMidpoint.geometry = postMidpointGeometry
        }
    }

    /**
     * Finishes the current drawing by finalizing the working graphic[s], resetting the drawing state, and notifying
     * the listener that the drawing has finished.
     */
    private fun finishDrawing() {
        // If current point is null then there is no drawing to finish
        if (mCurrentPoint != null) {
            when (mDrawingMode) {
                SketchGraphicsOverlay.DrawingMode.POINT -> {
                    // If we're drawing a point, set the symbol to the placed symbol and reset the current point
                    mCurrentPoint!!.symbol = mPointPlacedSymbol
                    mCurrentPoint = null
                    if (!mUndoElementStack.isEmpty()) {
                        // Remove any of the move graphic undo events. Once placed, undo should just remove the point
                        while (mUndoElementStack.peek().event == UndoRedoItem.Event.MOVE_POINT) {
                            mUndoElementStack.pop()
                        }
                    }
                }
                SketchGraphicsOverlay.DrawingMode.POLYGON -> {
                    // If we're drawing a polygon, logic is similar to finishing a polyline, but additionally need
                    // to remove the final midpoint
                    if (mGraphics.size > 0) {
                        mGraphics.removeAt(mGraphics.size - 1)
                    }
                    // Set the current point to the placed vertex symbol and set the line to the placed line symbol
                    mCurrentPoint!!.symbol = mPolylineVertexSymbol
                    mCurrentLine!!.symbol = mPolylinePlacedSymbol
                    // The second to last graphic is the final midpoint, and we need to remove all midpoints
                    var index = 0
                    if (mGraphics.size > 1) {
                        index = mGraphics.size - 2
                    }
                    // Pop events until all the add/move polyline point events are gone (once placed, we only want to remove
                    // a polyline on undo). The final popped event will be an ADD_GRAPHIC event, which will be replaced
                    // further down by an ADD_POLYLINE event
                    if (!mUndoElementStack.isEmpty()) {
                        var event: UndoRedoItem.Event
                        do {
                            event = mUndoElementStack.pop().event
                        } while (event == UndoRedoItem.Event.ADD_POLYLINE_POINT || event == UndoRedoItem.Event.MOVE_POLYLINE_POINT)

                        while (index > 0 && mGraphics[index].symbol == mPolylineMidpointSymbol) {
                            // For each add event, remove the midpoint and decrement the index
                            mGraphics.removeAt(index)
                            index -= 2
                        }
                        // Push a new event indicating that we've finished a POLYLINE
                        mUndoElementStack.add(UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE, ""))
                    }
                    // Reset the boolean and working graphics
                    mIsPolylineStarted = false
                    mCurrentPoint = null
                    mCurrentLine = null
                    mCurrentPolygon = null
                    mCurrentPointCollection = null
                    mIsMidpointSelected = false
                }
                SketchGraphicsOverlay.DrawingMode.POLYLINE -> {
                    mCurrentPoint!!.symbol = mPolylineVertexSymbol
                    mCurrentLine!!.symbol = mPolylinePlacedSymbol
                    var index = 0
                    if (mGraphics.size > 1) {
                        index = mGraphics.size - 2
                    }
                    if (!mUndoElementStack.isEmpty()) {
                        var event: UndoRedoItem.Event
                        do {
                            event = mUndoElementStack.pop().event
                        } while (event == UndoRedoItem.Event.ADD_POLYLINE_POINT || event == UndoRedoItem.Event.MOVE_POLYLINE_POINT)
                        while (index > 0 && mGraphics[index].symbol == mPolylineMidpointSymbol) {
                            mGraphics.removeAt(index)
                            index -= 2
                        }
                        mUndoElementStack.add(UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE, ""))
                    }
                    mIsPolylineStarted = false
                    mCurrentPoint = null
                    mCurrentLine = null
                    mCurrentPolygon = null
                    mCurrentPointCollection = null
                    mIsMidpointSelected = false
                }
            }
        }
        // Reset drawing mode and empty the redo stack
        mDrawingMode = DrawingMode.NONE
        clearStack(mRedoElementStack)
        mListener.onDrawingFinished()
    }

    /**
     * Represents the different possible drawing modes the SketchGraphicsOverlay can be in
     */
    enum class DrawingMode {
        POINT,
        POLYLINE,
        POLYGON,
        NONE
    }

    /**
     * Represents a single action that can be undone/redone in the sketching stack
     */
    class UndoRedoItem
    /**
     * Creates a new UndoRedoItem with the specified event type and optional object.

     * @param event   the type of event that occured
     * *
     * @param element optionally an object to help undo/redo the action
     */
    (// Each item has an event type and optionally an object to use in undoing/redoing the action
            /**
             * Gets the type of the event.

             * @return the type of the event
             */
            val event: UndoRedoItem.Event,
            /**
             * Gets the object with which to undo/redo the action (depending on the event type,
             * may be null).

             * @return the object with which to undo/redo the action, or null if there is none
             */
            val element: Any) {

        /**
         * Indicates different types of events that can occur.
         */
        enum class Event {
            ADD_POINT,
            MOVE_POINT,
            REMOVE_POINT,
            ADD_POLYLINE_POINT,
            MOVE_POLYLINE_POINT,
            REMOVE_POLYLINE_POINT,
            ADD_POLYLINE,
            REMOVE_POLYLINE,
            ERASE_GRAPHICS,
            REPLACE_GRAPHICS
        }

        /**
         * Represents the specific action of moving a polyline point, which additionally needs
         * to indicate if the point moved was a midpoint.
         */
        class MovePolylinePointElement
        /**
         * Instantiates a new MovePolylinePointElement.

         * @param graphic the graphic of the moved point
         * *
         * @param point the position of the moved point
         * *
         * @param isMidpoint true if the moved point was a midpoint
         */
        (graphic: Graphic, point: Point, isMidpoint: Boolean) {
            /**
             * Gets the graphic of the moved point.

             * @return the graphic of the moved point
             */
            var graphic: Graphic
                internal set
            /**
             * Gets the position of the moved point (note this is required because the Point
             * returned by graphic.getGeometry() will have changed by reference).

             * @return the position of the moved point
             */
            var point: Point
                internal set
            /**
             * Checks if the moved point was a midpoint.

             * @return true if the moved point was a midpoint
             */
            var isMidpoint: Boolean = false
                internal set

            init {
                this.graphic = graphic
                this.point = point
                this.isMidpoint = isMidpoint
            }
        }
    }

    /**
     * A custom MapViewOnTouchListener that handles drawing events on the SketchGraphicsOverlay
     */
    private inner class DrawingMapViewOnTouchListener
    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified context and MapView.

     * @param context the application context from which to get the display metrics
     * *
     * @param mapView the MapView on which to control touch events
     */
    (context: Context, mapView: MapView) : DefaultMapViewOnTouchListener(context, mapView) {

        // Boolean flags to indicate whether we've chosen a midpoint or not and if we've started dragging it
        private var mVertexDragStarted = false

        override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
            // get the screen point where user tapped
            val screenPoint = android.graphics.Point(event!!.x.toInt(), event.y.toInt())

            // identify graphics on the sketch graphics overlay
            val identifyGraphic = mMapView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false)

            identifyGraphic.addDoneListener {
                try {
                    // get the list of graphics returned by identify
                    val identifyResult = identifyGraphic.get()
                    val graphic = identifyResult.graphics

                    // In order to put new points inside a previously drawn or currently drawing polygon, don't trigger
                    // on clicking a polygon graphic
                    if (!graphic.isEmpty() && graphic[0].geometry !is Polygon) {
                        // Clicking a graphic only changes the current point if we're drawing a polyline or polygon
                        if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
                            // Get the graphic we selected
                            val g = graphic[0]
                            // If we clicked a point other than the point we're currently working with..
                            if (mCurrentPoint != null && mCurrentPoint != g) {
                                // If the last thing we had was a midpoint and we never moved it, set its symbol back to a midpoint
                                if (mIsMidpointSelected && !mVertexDragStarted) {
                                    mCurrentPoint!!.symbol = mPolylineMidpointSymbol
                                } else {
                                    // If it wasn't a midpoint or we moved it, change it to a placed vertex symbol.
                                    mCurrentPoint!!.symbol = mPolylineVertexSymbol
                                }
                                // If the selected graphic has the midpoint symbol, take note that we selected a midpoint
                                mIsMidpointSelected = g.symbol == mPolylineMidpointSymbol
                                mVertexDragStarted = false
                                // Set our current working point to the selected graphic and change its symbol to the placing symbol
                                mCurrentPoint = g
                                mCurrentPoint!!.symbol = mPointPlacementSymbol
                            }
                        }
                    } else {
                        // Check if the graphics list was empty before we add our point
                        val graphicsWasEmpty = mGraphics.isEmpty()
                        // If we didn't click an existing graphic, add a new point to the current drawing
                        val point = mMapView.screenToLocation(screenPoint)
                        if (mDrawingMode == DrawingMode.POINT) {
                            if (mCurrentPoint == null) {
                                // If this is the first click after setting drawing mode to point, add a new grahpic
                                mCurrentPoint = Graphic(point, mPointPlacementSymbol)
                                mGraphics.add(mCurrentPoint!!)
                                val graphics = ArrayList<Graphic>()
                                graphics.add(mCurrentPoint!!)
                                // Push a new event indicating that we've added a graphic
                                queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics))
                            } else {
                                // If we've already placed a point, clicking a new location will move the point
                                // Queue a new event indicating we've moved a graphic
                                queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, mCurrentPoint!!.geometry))
                                mCurrentPoint!!.geometry = point
                            }
                        } else if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
                            // If we're drawing a polyline or polygon, we need to add a point to the point collection
                            mIsMidpointSelected = false
                            if (!mIsPolylineStarted) {
                                // If this is the first point of a polyline
                                mCurrentPointCollection!!.add(point)
                                if (mDrawingMode == DrawingMode.POLYGON) {
                                    // If it's a polygon, add a final point as the start point so the polyline draws a complete polygon
                                    mCurrentPointCollection!!.add(point)
                                }
                            } else {
                                // If we've already started the polyline/polygon...
                                if (mDrawingMode == DrawingMode.POLYGON) {
                                    // If it's a polygon and there are at least 3 points, then we need to remove the last graphic (which is
                                    // the midpoint of the last line segment) so that we can get the midpoint of the new segment and add it
                                    if (mCurrentPointCollection!!.size > 2) {
                                        mGraphics.removeAt(mGraphics.size - 1)
                                    }
                                    // Add the new point before the last point (so the polyline draws completely around the polygon)
                                    mCurrentPointCollection!!.add(mCurrentPointCollection!!.size - 1, point)
                                } else {
                                    // If we're drawing a polyline just add it to the end
                                    mCurrentPointCollection!!.add(point)
                                }
                            }
                            // If this is the first point set up the point, line and polygon grahpics
                            if (!mIsPolylineStarted) {
                                // Create a new polyline and point
                                mCurrentLine = Graphic(Polyline(mCurrentPointCollection), mPolylinePlacementSymbol)
                                mCurrentPoint = Graphic(point, mPointPlacementSymbol)
                                //
                                val graphics = ArrayList<Graphic>()
                                // If we're drawing a polygon, also create a polygon graphic
                                if (mDrawingMode == DrawingMode.POLYGON) {
                                    mCurrentPolygon = Graphic(Polygon(mCurrentPointCollection), mPolygonFillSymbol)
                                    // Add it first so the line and points draw on top of it
                                    mGraphics.add(mCurrentPolygon!!)
                                    graphics.add(mCurrentPolygon!!)
                                }
                                // Add the line first so points drop on top of it
                                mGraphics.add(mCurrentLine!!)
                                mGraphics.add(mCurrentPoint!!)
                                graphics.add(mCurrentLine!!)
                                graphics.add(mCurrentPoint!!)
                                // Queue a new event indicating we've added a point
                                queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics))
                                mIsPolylineStarted = true
                            } else {
                                // If we've already started the line, just add the polyline point
                                addPolylinePoint(point)
                                queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE_POINT, ""))
                            }
                        }
                        val graphicsIsEmpty = mGraphics.isEmpty()
                        // If the graphics list was previously empty and now it's not, notify the listener
                        // to enable the clear button
                        if (graphicsWasEmpty && !graphicsIsEmpty) {
                            mListener.onClearStateChanged(true)
                            // If previous non empty and now it is, notify listener to disable the clear button
                        } else if (!graphicsWasEmpty && graphicsIsEmpty) {
                            mListener.onClearStateChanged(false)
                        }
                        // Any time we add a new graphic, clear the redo stack since we should only be able to
                        // do redos directly after undos
                        clearStack(mRedoElementStack)
                    }
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                } catch (ie: ExecutionException) {
                    ie.printStackTrace()
                }
            }
            return true
        }

        override fun onLongPress(event: MotionEvent) {
            // Long press finishes a drawing
            finishDrawing()
        }

        override fun onScroll(from: MotionEvent, to: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            // Assume that we're going to call the super method (for panning)
            var callSuper = true
            // If we don't have a current point we're just going to call super
            if (mCurrentPoint != null) {
                // If we do have a current working point, check to see if we're we are dragging from is close to our working graphic
                val currentPoint = mMapView.locationToScreen(mCurrentPoint!!.geometry as Point)
                val fromPoint = android.graphics.Point(from.x.toInt(), from.y.toInt())
                val dx = currentPoint.x - fromPoint.x
                val dy = currentPoint.y - fromPoint.y
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
                if (distance < 20) {
                    // If it is, don't call the super method since we'll be moving our working point
                    callSuper = false
                    // Get the location point that we're moving to
                    val toPoint = android.graphics.Point(to!!.x.toInt(), to.y.toInt())
                    val oldGeometry = mCurrentPoint!!.geometry as Point
                    // Make a copy of the current geometry so that changes to the previous geometry don't update by reference
                    val oldPointCopy = Point(oldGeometry.x, oldGeometry.y, mMapView.spatialReference)
                    val newGeometry = mMapView.screenToLocation(toPoint)
                    // If this is the first move event after clicking down, mark the current position so we can undo the move event
                    if (!mVertexDragStarted) {
                        // Queue a new event indicating that we've moved a point (or polyline point)
                        if (mDrawingMode == DrawingMode.POINT) {
                            queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, oldPointCopy))
                        } else {
                            queueUndoRedoItem(mUndoElementStack, UndoRedoItem(UndoRedoItem.Event.MOVE_POLYLINE_POINT,
                                    UndoRedoItem.MovePolylinePointElement(mCurrentPoint!!, oldPointCopy, mIsMidpointSelected)))
                        }
                    }
                    // If we're drawing a polyline or polygon, move the current point and the attached line segments
                    if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
                        // get the index of the current working graphic
                        val graphicIndex = mGraphics.indexOf(mCurrentPoint!!)
                        val pointIndex: Int
                        // If our current working graphic is a midpoint and this is the first event of dragging it, we'll need
                        // to add a new vertex where the midpoint was and create two new midpoints to the surrounding points
                        if (mIsMidpointSelected && !mVertexDragStarted) {
                            splitMidpoint(newGeometry)
                        } else {
                            // If it's not a midpoint, then just find the index of the vertex point
                            pointIndex = mCurrentPointCollection!!.indexOf(mCurrentPoint!!.geometry)
                            // Update the location of the selected point to the new geometry and update the line graphic
                            mCurrentPointCollection!![pointIndex] = newGeometry
                            // Get the midpoint before this point so it can move with the line segment
                            val preMidpoint = if (pointIndex == 0) null else mGraphics[graphicIndex - 1]
                            // If it's not null (only null if this is the first point in the line)...
                            if (preMidpoint != null) {
                                // Get the new midpoint location and update the graphic's geometry
                                val preMidpointGeometry = getMidpoint(mCurrentPointCollection!![pointIndex - 1], newGeometry)
                                preMidpoint.geometry = preMidpointGeometry
                            }
                            // Get the midpoint after this point so it can move with the line segment
                            val postMidpoint = if (pointIndex == mCurrentPointCollection!!.size - 1) null else mGraphics[graphicIndex + 1]
                            // If it's not null (only null if this is the last point in the line)...
                            if (postMidpoint != null) {
                                // Get the new midpoint location and update the graphic's geometry
                                val postMidpointGeometry = getMidpoint(newGeometry, mCurrentPointCollection!![pointIndex + 1])
                                postMidpoint.geometry = postMidpointGeometry
                            }
                            // If we're drawing a polygon we also need to update the polygon geometry and final midpoint
                            if (mDrawingMode == DrawingMode.POLYGON) {
                                if (pointIndex == 0 || pointIndex == mCurrentPointCollection!!.size - 2) {
                                    // If we're moving the first point, we need to replicate the change in the duplicate final point
                                    if (pointIndex == 0) {
                                        mCurrentPointCollection!![mCurrentPointCollection!!.size - 1] = newGeometry
                                    }
                                    updatePolygonMidpoint()
                                }
                                mCurrentPolygon!!.geometry = Polygon(mCurrentPointCollection)
                            }
                            mCurrentLine!!.geometry = Polyline(mCurrentPointCollection)
                        }
                    }
                    // Indicate that we've started the point drag
                    mVertexDragStarted = true
                    // Finally update the geometry up the current point
                    mCurrentPoint!!.geometry = newGeometry
                    // Any time we add a new graphic, clear the redo stack since we should only be able to
                    // do redos directly after undos
                    clearStack(mRedoElementStack)
                }
            }
            // If we didn't do a point drag, call super to pan the map
            if (callSuper) {
                super.onScroll(from, to, distanceX, distanceY)
            }
            return true
        }

        override fun onUp(event: MotionEvent?): Boolean {
            // Reset the drag started flag when the pointer is lifted
            mVertexDragStarted = false
            return true
        }
    }
}
