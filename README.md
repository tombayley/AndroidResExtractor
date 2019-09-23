# Android Res Extractor

[![](https://jitpack.io/v/tombayley/AndroidResExtractor.svg)](https://jitpack.io/#tombayley/AndroidResExtractor)

A library to filter and extract Android resources from all locales (only supports strings at this current time).

Useful if you want to extract certain resources from one project to another or similar.




## Usage
Add to your own project using Gradle or create your own script using the JAR file:

##### Using gradle dependency (/example/)
1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```
2. Add the dependency:
```
dependencies {
    implementation 'com.github.tombayley.AndroidResExtractor:androidresextractor:1.0.0'
}
```

##### Using JAR file (/example_jar/)
1. To run you program with the jar file:
`java -cp ".;androidresextractor-1.0.0.jar" example_jar/MyExtractor.java`



### Creating your program
1. Add the following code to your class:
```java
import com.tombayley.androidresextractor.Extractor;

//...

HashMap<String, String> stringsToExtract = new HashMap<>();
stringsToExtract.put("other_project_string_id", "string_id_to_save_as");
stringsToExtract.put("other_project_string_id_two", "string_id_to_save_as_two");

Extractor.ExtractorBuilder extractorBuilder = new Extractor.ExtractorBuilder()
        .setResourcesToExtract(stringsToExtract)
        .setResourcesPath("resources_dir/android_resources")
        .setOutputPath("resources_dir/output")
        .setOutputFileName("extracted_strings.xml");

Extractor extractor = new Extractor(extractorBuilder);
extractor.start();
```

2. You can customise the ExtractorBuilder as you like:

| Config | Explanation |
| --- | --- |
| setResourcesToExtract() | A Map of attribute names of elements to extract (key) and a new attribute name to be used to save the extracted element with (value). See the "stringsToExtract" map above |
| setResourcesPath() | Path to directory containing existing Android resources |
| setOutputPath() | Output path of extracted xml |
| setOutputFileName() | File name used to save extracted elements for each locale |



## Examples
See [/example/](https://github.com/tombayley/AndroidResExtractor/tree/master/example/src/main) for an an example usage.

This example extracts some of the names of quick setting tiles in the Android notification shade using resources from AOSP.

These resources are simply copied and pasted in this example in the [resources directory](https://github.com/tombayley/AndroidResExtractor/tree/master/example/src/main/resources/android_resources).

[Here](https://github.com/tombayley/AndroidResExtractor/blob/master/example/src/main/java/com/tombayley/androidresextractorexample/MyExtractor.java)
is the class used to configure and run the extractor.

The string attribute id's of the quick setting tile names are added to the "stringsToExtract" map.
The AOSP resources contain the string id's: "mobile_data", "quick_settings_hotspot_label", "status_bar_airplane" etc.
These are defined as the 'key' of each map entry.

The 'value' of each map entry is the string attribute id you want to save each string as. In the example, "tile_label_" is prepended for consistency(since they are all tile labels).

After finishing setting up the builder and running the program, it produces [this output](https://github.com/tombayley/AndroidResExtractor/tree/master/example/src/main/resources/output).
Have a look in some directories and files. You can see the desired AOSP string values have been extracted and saved using new attribute id's.




## TODO
- Automated unit tests
- Support for other resource files
- Combination of existing resource files with ones generated with this library
- User interface bundled with JAR
