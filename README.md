# Overview arcgis-android-sdk-kotlin-samples
ArcGIS Runtime SDK for Android v100.0.0 samples.  The `master` branch of this repository contains sample app modules for the latest available version of the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [repository releases](https://github.com/Esri/arcgis-runtime-samples-android/releases).

# Prerequisites
* The samples are building with `compileSdkVersion 25` which requires [Kotlin 1.1.2-4](https://kotlinlang.org/)
* [Android Studio 3.0 or later](http://developer.android.com/sdk/index.html)

# Developer Instructions
The **ArcGIS Android SDK Samples** are [Gradle](https://www.gradle.org) based Android projects which can be directly cloned and imported into Android Studio.

The latest ArcGIS Android SDK compile dependency is defined for all sample modules in the root project build.gradle.  This is the only place where you need to define the dependency to the ArcGIS Android SDK.

```groovy
subprojects{
    afterEvaluate {project ->
        if(project.hasProperty("dependencies")){
            dependencies {
                compile 'com.esri.arcgisruntime:arcgis-android:100.0.0'
                compile "org.jetbrains.kotlin:kotlin-stdlib-jre7:$kotlin_version"
            }
        }
    }
}
```

Our SDK is hosted in our public maven repository hosted by Bintray.  Our repository url is added to the projects root build.gradle file.

```groovy
repositories {
    jcenter()
    maven {
        url 'https://esri.bintray.com/arcgis'
    }
}
```
```
See the project's build.gradle and app's build.gradle.
```

## Fork the ArcGIS Android SDK gradle samples repo
If you haven't already, fork the [this repo](https://github.com/Esri/arcgis-android-sdk-gradle-samples/fork).

## Fork the Kotlin Samples repo
[fork repo](https://github.com/AshinJiang/arcgis-android-sdk-kotlin-sample/fork).

## Clone the repo

### Android Studio
Clone the **ArcGIS Android SDK Samples** in Android Studio:

1. Choose **VCS > Checkout from Version Control > GitHub** on the main menu.
2. From the **Repository** drop-down list, select the source repository to clone the data from.
3. In the **Folder** text box, specify the directory where the local repository for cloned sources will be set up.
4. Click the Clone button to start cloning the sources from the specified remote repository.

**NOTE**: Do not import the project into Android Studio.  There is an [outstanding issue](https://groups.google.com/forum/#!topic/adt-dev/o8h3Jg9ICGo) in Android Studio that requires importing the project in the steps defined below.

## Import Gradle Sample project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**, or **Import Non-Android Studio project** from the Welcome Quick Start.
* Navigate to the root project folder, **arcgis-runtime-samples-android** directory and click **OK**

## Run a sample
You should now be able to run any of the included samples.  We will use the `set-map-initial-location` sample as an example.

* Select `set-map-initial-location` from the **Select Run/Debug Configuration** drop down
* Click the **Run** button


[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Kotlin)​
