package com.example.linkedinscraper.config;

public class ApifyConfig {
    public static String apify_token = "apify_api_VNFP6tIq9ZfnlLkSKggIqbisWNEz6f2cAj74";
    public static String runActorUrlAsync = "https://api.apify.com/v2/acts/gdbRh93zn42kBYDyS/runs?token="+apify_token+"&memory=1024";
    public static String runActorUrlSync = "https://api.apify.com/v2/acts/gdbRh93zn42kBYDyS/run-sync-get-dataset-items?token="+apify_token+"&memory=1024";
    public static String dataSetUrl_1 = "https://api.apify.com/v2/datasets/";
    public static String dataSetUrl_2 = "/items?token="+apify_token+"&format=json";
    public static int NUM_RUNS = 5;

    public static String getDataSetUrl(String dataSetId){
        return dataSetUrl_1+dataSetId+dataSetUrl_2;
    }
}
