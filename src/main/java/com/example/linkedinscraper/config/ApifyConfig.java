package com.example.linkedinscraper.config;

public class ApifyConfig {
    public static String apify_token = "apify_api_fnc64GgkJwkLDGbDAmX0cXFjxXFsgL433hJd";
    public static String runActorUrlAsync = "https://api.apify.com/v2/acts/gdbRh93zn42kBYDyS/runs?token="+apify_token+"&memory=1024&timeout=120";
    public static String runActorUrlSync = "https://api.apify.com/v2/acts/gdbRh93zn42kBYDyS/run-sync-get-dataset-items?token="+apify_token+"&memory=1024";
    public static String dataSetUrl_1 = "https://api.apify.com/v2/datasets/";
    public static String dataSetUrl_2 = "/items?token="+apify_token+"&format=json";
    public static int NUM_RUNS = 5;


    public static String smartleadGetAllCampaigns = "https://server.smartlead.ai/api/v1/campaigns?api_key=2151a558-db11-4c24-b67d-c6b080c073f6_wurxhw4";
    public static String smartleadGetCampaign1 = "https://server.smartlead.ai/api/v1/campaigns/";
    public static String smartleadGetCampaign2 = "/analytics-by-date?api_key=2151a558-db11-4c24-b67d-c6b080c073f6_wurxhw4";

    public static String getCampaignUrlByDate(String campaignId, String date){
        return smartleadGetCampaign1+campaignId+smartleadGetCampaign2+"&start_date="+date+"&end_date="+date;
    }

    public static String getDataSetUrl(String dataSetId){
        return dataSetUrl_1+dataSetId+dataSetUrl_2;
    }
}
