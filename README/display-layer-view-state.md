# Display Layer View State
The sample demonstrates how to view the status of the layers on the map. This is obtained from the enum value of ```LayerViewStatus```. A signal handler is set up on the map to handle the ```LayerViewStateChangedEvent``` signal, and the status text is updated when the status changes. The list of operational layers in the map are displayed. Each row in this list also has the view status corresponding to that layer. Pan or zoom the map, to view the status changes in the layers.

![Display Layer View State](display-layer-view-state.png)

* ArcGISMap
* MapView
* LayerViewStateChangedListener
* ArcGISTiledLayer
* ArcGISMapImageLayer
* ServiceFeatureTable
* FeatureLayer

# Developer Pattern

## Java
The ```addLayerViewStateChangedListener``` on **MapView** listens for `LayerViewStateChangedEvent`. To get the layer's view status, use method `getLayerViewStatus().iterator().next()` on event.

```java
// Listen to changes in the status of the Layer
mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
    @Override
    public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {

        // get the layer which changed it's state
        Layer layer = layerViewStateChangedEvent.getLayer();

        // get the View Status of the layer
        // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, UNKNOWN
        String viewStatus = layerViewStateChangedEvent.getLayerViewStatus().iterator().next().toString();

        final int layerIndex = mMap.getOperationalLayers().indexOf(layer);

        // finding and updating status of the layer
        switch (layerIndex) {
            case TILED_LAYER:
                timeZoneTextView.setText(viewStatusString(viewStatus));
                break;
            case IMAGE_LAYER:
                worldCensusTextView.setText(viewStatusString(viewStatus));
                break;
            case FEATURE_LAYER:
                recreationTextView.setText(viewStatusString(viewStatus));
                break;
        }

    }
});
```

## Kotlin
```kotlin
// Listen to changes in the status of the Layer
mMapView!!.addLayerViewStateChangedListener { layerViewStateChangedEvent ->
    // get the layer which changed it's state
    val layer = layerViewStateChangedEvent.layer

    // get the View Status of the layer
    // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, UNKNOWN
    val viewStatus = layerViewStateChangedEvent.layerViewStatus.iterator().next().toString()

    val layerIndex = mMap.operationalLayers.indexOf(layer)

    // finding and updating status of the layer
    when (layerIndex) {
        TILED_LAYER -> timeZoneTextView!!.text = viewStatusString(viewStatus)
        IMAGE_LAYER -> worldCensusTextView!!.text = viewStatusString(viewStatus)
        FEATURE_LAYER -> recreationTextView!!.text = viewStatusString(viewStatus)
    }
}
```
