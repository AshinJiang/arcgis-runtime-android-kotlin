# Show Callout
The **Show Callout** sample draws a callout on a MapView and manages its behavior. A callout displays an Android View that contains text and/or other content. It has a leader that points to the location the callout refers to. 

![Show Callout App](show-callout.png)

## Features
* ArcGISMap
* MapView
* Callout

## Developer Pattern
The pattern demonstrates how to show location coordinates on a `MapView` using a `Callout`.  The sample converts screen coordinates to location coordinates to show the location of the callout.  The content of the callout is created programmatically by an Android `TextView`.

### Java
```java
// create a textview for the callout
TextView calloutContent = new TextView(getApplicationContext());
calloutContent.setTextColor(Color.BLACK);
calloutContent.setSingleLine();
calloutContent.setText("X:" +  (String.format("%.2f", singleTapPoint.getX()))
        + ", y:" + (String.format("%.2f", singleTapPoint.getY())));

// get callout, set content and show
mCallout = mMapView.getCallout();
mCallout.setLocation(singleTapPoint);
mCallout.setContent(calloutContent);
mCallout.show();
```

### Kotlin
```kotlin
// create a textview for the callout
val calloutContent = TextView(applicationContext)
calloutContent.setTextColor(Color.BLACK)
calloutContent.setSingleLine()
calloutContent.text = "X:" + String.format("%.2f", singleTapPoint.x)+
                        ", y:" + String.format("%.2f", singleTapPoint.y)

// get callout, set content and show
mCallout = mMapView.callout
mCallout!!.location = singleTapPoint
mCallout!!.content = calloutContent
mCallout!!.show()
```