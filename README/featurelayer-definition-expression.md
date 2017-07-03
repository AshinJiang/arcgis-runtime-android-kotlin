#Feature layer definition expression
This sample demonstrates how you can limit which features to display on the map. Use the buttons in the bottom toolbar to apply or reset definition expression.

![FeatureLayer Definition Expression](featurelayer-definition-expression.png)

## How it works

You can achieve this by setting the definition expression property on a feature layer. It is the syntax of a SQL where clause by which to limit which features are displayed on the map.

## Developer Pattern

### Java
```java
private void applyDefinitionExpression() {
    // apply a definition expression on the feature layer
    // if this is called before the layer is loaded, it will be applied to the loaded layer
    mFeatureLayer.setDefinitionExpression("req_Type = 'Tree Maintenance or Damage'");
}

private void resetDefinitionExpression() {
    // set the definition expression to nothing (empty string, null also works)
    mFeatureLayer.setDefinitionExpression("");
}
```

### Kotlin
```kotlin
rivate fun applyDefinitionExpression() {
    // apply a definition expression on the feature layer
    // if this is called before the layer is loaded, it will be applied to the loaded layer
    mFeatureLayer!!.definitionExpression = "req_Type = 'Tree Maintenance or Damage'"
}

private fun resetDefinitionExpression() {
    // set the definition expression to nothing (empty string, null also works)
    mFeatureLayer!!.definitionExpression = ""
}
```