package com.tombayley.androidresextractorexample;

import com.tombayley.androidresextractor.Extractor;

import java.util.HashMap;

public class MyExtractor {

    public static void main(String[] args) {
        new MyExtractor();
    }

    public MyExtractor() {
        final String PREFIX = "tile_label_";
        HashMap<String, String> stringsToExtract = new HashMap<>();
        stringsToExtract.put("mobile_data", PREFIX + "mobile_data");
        stringsToExtract.put("quick_settings_hotspot_label", PREFIX + "hotspot");
        stringsToExtract.put("status_bar_airplane", PREFIX + "airplane_mode");
        stringsToExtract.put("battery_detail_switch_title", PREFIX + "battery_saver");
        stringsToExtract.put("quick_settings_location_label", PREFIX + "location");
        stringsToExtract.put("quick_settings_location_off_label", PREFIX + "location_off");
        stringsToExtract.put("quick_settings_nfc_label", PREFIX + "nfc");
        stringsToExtract.put("data_saver", PREFIX + "data_saver");
        stringsToExtract.put("quick_settings_inversion_label", PREFIX + "invert_colors");

        Extractor.ExtractorBuilder extractorBuilder = new Extractor.ExtractorBuilder()
                .setResourcesToExtract(stringsToExtract)
                .setResourcesPath("example/src/main/resources/android_resources")
                .setOutputPath("example/src/main/resources/output")
                .setOutputFileName("tile_labels.xml");

        Extractor extractor = new Extractor(extractorBuilder);
        extractor.start();
    }

}
