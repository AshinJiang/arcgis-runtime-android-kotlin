# Identify Graphics Hit Test

![Identify Graphic Overlay App](identify-graphic-overlay.png)

The **Identify Graphics Hit Test** sample demonstrates how to create a ```Graphic``` and add it to a ```GraphicOverlay``` where it can be identified from the ```MapView```.

## Features
* ArcGISMap
* MapView
* DefaultMapViewOnTouchListener
* Graphic
* GraphicsOverlay

## Developer Pattern
### Graphics
Graphics represent graphic elements used in graphic overlays on the ```MapView```. Graphics are contained in a modifiable ```List``` for graphics on a ```GraphicsOverlay```.  The ```ListenableList``` interface defines a listenable ```List``` of graphics that can be notified when items are being added or removed from the ```List```.

#### Java
```java
// create list of graphics
ListenableList<Graphic> graphics = grOverlay.getGraphics();
// add graphic to graphics overlay
graphics.add(graphic);
// add graphics overlay to the MapView
mMapView.getGraphicsOverlays().add(grOverlay);
```

#### Kotlin
```kotlin
        // create graphics overlay
        grOverlay = GraphicsOverlay()
        // create list of graphics
        val graphics = grOverlay!!.graphics
        // add graphic to graphics overlay
        graphics.add(graphic)
        // add graphics overlay to the MapView
        mMapView!!.graphicsOverlays.add(grOverlay)
```

### Identify Graphics Overlay
You can identify on a graphics overlay from the ```MapView.identifyOnGraphicsOverlay()``` method which returns a ```ListenableFuture<List<Graphic>>```.  Futures are an abstraction for asynchronous event driven systems that the Android platform provides.  The method returns immediately as a wrapper around the ```List<Graphic>```, once the asynchronous event is completed you can access it with ```ListenableFuture.get()```.

#### Java
```java
// identify graphics on the graphics overlay
final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(grOverlay, screenPoint, 10.0, false, 2);

identifyGraphic.addDoneListener(new Runnable() {
    @Override
    public void run() {
        try {
            IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
            // get the list of graphics returned by identify graphic overlay
            List<Graphic> graphic = grOverlayResult.getGraphics();
            // get size of list in results
            int identifyResultSize = graphic.size();
            if(!graphic.isEmpty()){
                // show a toast message if graphic was returned
                Toast.makeText(getApplicationContext(), "Tapped on " + identifyResultSize + " Graphic", Toast.LENGTH_SHORT).show();
            }
        }catch(InterruptedException | ExecutionException ie){
            ie.printStackTrace();
        }

    }
});
```

#### Kotlin
```kotlin
            // identify graphics on the graphics overlay
            val identifyGraphic = mMapView.identifyGraphicsOverlayAsync(grOverlay, screenPoint, 10.0, false, 2)

            identifyGraphic.addDoneListener {
                try {
                    val grOverlayResult = identifyGraphic.get()
                    // get the list of graphics returned by identify graphic overlay
                    val graphic = grOverlayResult.graphics
                    // get size of list in results
                    val identifyResultSize = graphic.size
                    if (!graphic.isEmpty()) {
                        // show a toast message if graphic was returned
                        Toast.makeText(applicationContext, "Tapped on $identifyResultSize Graphic", Toast.LENGTH_SHORT).show()
                    }
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                } catch (ie: ExecutionException) {
                    ie.printStackTrace()
                }
            }
```