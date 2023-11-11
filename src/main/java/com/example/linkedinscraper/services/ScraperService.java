package com.example.linkedinscraper.services;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.example.linkedinscraper.payloads.ActorRunRequest;
import com.example.linkedinscraper.payloads.JobDataSetResponse;
import com.example.linkedinscraper.payloads.JobQueryRequest;
import com.example.linkedinscraper.repositories.TamCompaniesJobQueriesRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.example.linkedinscraper.config.ApifyConfig.*;

@Service
public class ScraperService {

    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    private final TamCompaniesJobQueriesRepository queriesRepository;
    public ScraperService(RestTemplate restTemplate, TamCompaniesJobQueriesRepository queriesRepository) {
        this.restTemplate = restTemplate;
        this.queriesRepository = queriesRepository;
    }

    public List<JobDataSetResponse> queryCompany(JobQueryRequest jobQueryRequest) {
        String url = buildSearchUrl(jobQueryRequest);
        JsonNode runResponse = runActorSync(url);
        //JsonNode jobs = getJobs(runResponse);
        List<JobDataSetResponse> jobsList = parseJobs(runResponse);
        return filterKeywords(jobsList, jobQueryRequest);
    }
    public JsonNode runActorSync(String searchUrl) {
        ActorRunRequest runRequest = new ActorRunRequest(searchUrl);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode requestNode = mapper.valueToTree(runRequest);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    runActorUrlSync,
                    HttpMethod.POST,
                    new HttpEntity<>(requestNode),
                    JsonNode.class
            );
            //logger.info("Response: \n" + responseEntity);
            return responseEntity.getBody();
        } catch (Exception e) {
            logger.info(e.toString());
            return JsonNodeFactory.instance.textNode(e.getMessage());
        }
    }

    public List<JobDataSetResponse> filterKeywords(List<JobDataSetResponse> jobDataSetResponses, JobQueryRequest jobQueryRequest) {
        jobDataSetResponses = jobDataSetResponses.stream().filter(job -> {
            System.out.println("Job Id: " + job.getId());
            boolean match = false;
            String keyMatched = strContains(job.getTitle(), jobQueryRequest.getKeywordsInTitle());
            if (!keyMatched.isEmpty()) {
                //System.out.println("T Keyword: " + keyMatched);
                match = true;
            }
            else{
                keyMatched = strContains(job.getDescriptionText(), jobQueryRequest.getKeywordsInBody());
                if (!keyMatched.isEmpty()) {
                    //System.out.println("B Keyword: " + keyMatched);
                    match = true;
                }
            }
            if (match) {

                keyMatched = strContains(job.getTitle(), jobQueryRequest.getKeywordsNotInTitleAndBody());
                if (!keyMatched.isEmpty()) {
                    //System.out.println("^TKeyword: " + keyMatched);
                    match = false;
                }

                keyMatched = strContains(job.getDescriptionText(), jobQueryRequest.getKeywordsNotInTitleAndBody());
                if (!keyMatched.isEmpty()) {
                    //System.out.println("^BKeyword: " + keyMatched);
                    match = false;
                }
            }

            return match;
        }).toList();
        System.out.println("Jobs Filtered " + jobDataSetResponses.size());
        return jobDataSetResponses;
    }

    public static String strContains(String inputStr, ArrayList<String> items) {
        for (String item : items) {
            if (inputStr.toLowerCase().matches(".*\\b"+item.toLowerCase()+"\\b.*")) {
                return item;
            }
        }
        return "";
    }

    public List<JobDataSetResponse> parseJobs(JsonNode jobs) {
//        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
//                getDataSetUrl("IOEOSCZqKL0qky5M4"),
//                HttpMethod.GET,
//                new HttpEntity<>(null),
//                JsonNode.class
//        );
        List<JobDataSetResponse> jobsList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (jobs.isArray()) {
            System.out.println("Jobs unParsed Array: " + jobs.size());
            for (JsonNode node : jobs) {
                JobDataSetResponse jobDataSetResponse;
                try {
                    jobDataSetResponse = objectMapper.treeToValue(node, JobDataSetResponse.class);
                    jobsList.add(jobDataSetResponse);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
            System.out.println("Jobs Parsed " + jobsList.size());
        }
        return jobsList;
    }

    private JsonNode getJobs(JsonNode response) {
        try {
            String dataSetId = null;
            if (response != null) {
                if (response.get("data") != null) {
                    JsonNode data = response.get("data");
                    if (data.get("status").asText().equals("READY") || data.get("status").asText().equals("SUCCEEDED")) {
                        dataSetId = data.get("defaultDatasetId").asText();
                        if (dataSetId != null && !dataSetId.isEmpty()) {
                            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                                    getDataSetUrl(dataSetId),
                                    HttpMethod.GET,
                                    new HttpEntity<>(JsonNodeFactory.instance.nullNode()),
                                    JsonNode.class
                            );
                            return responseEntity.getBody();
                        }
                    } else {
                        return JsonNodeFactory.instance.textNode("Request Timed Out. Run Not Ready");
                    }
                }
                return JsonNodeFactory.instance.textNode("Request Timed Out. Data Null.");
            }
        } catch (Exception e) {
            logger.info(e.toString());
            return JsonNodeFactory.instance.textNode("Error fetching dataset " + e.getMessage());
        }
        return JsonNodeFactory.instance.textNode("Error fetching dataset.");
    }

    public String buildSearchUrl(JobQueryRequest jobQueryRequest) {
        StringBuilder search = new StringBuilder();
        search.append("https://www.linkedin.com/jobs/search/?f_C=")
                .append(jobQueryRequest.getCompanyLinkedinUid());
        if (jobQueryRequest.getPostedIn()!=null){
            search.append("&f_TPR=r");
            search.append(jobQueryRequest.getPostedIn().getVal());
        }
        search.append("&geoId=103644278");//united states
        search.append("&keywords=").append(jobQueryRequest.getKeywordsInTitle().get(0));
        //search.append("&location=United%20States");
        System.out.println("query: "+search.toString());
        return search.toString();
    }

    public TamCompaniesJobQueries saveQuery(JobQueryRequest payload) {
        TamCompaniesJobQueries queryObj = TamCompaniesJobQueries.builder()
                .tamCompanyId(payload.getTamCompanyId())
                .companyLinkedinUid(payload.getCompanyLinkedinUid())
                .postedBeforeDate(payload.getPostedBeforeDate())
                .postedAfterDate(payload.getPostedAfterDate())
                .status("IMPORTED")
                .build();
        if (!payload.getKeywordsInTitle().isEmpty()){
            queryObj.setKeysTitle(StringUtils.join(payload.getKeywordsInTitle(),','));
        }
        if (!payload.getKeywordsInBody().isEmpty()){
            queryObj.setKeysBody(StringUtils.join(payload.getKeywordsInBody(),','));
        }
        if (!payload.getKeywordsNotInTitleAndBody().isEmpty()){
            queryObj.setKeysNot(StringUtils.join(payload.getKeywordsNotInTitleAndBody(),','));
        }
        if (payload.getPostedIn()!=null){
            queryObj.setPostedIn(payload.getPostedIn().name());
        }
        return queriesRepository.save(queryObj);
    }

    private void runActorASync(String url, TamCompaniesJobQueries request) {
        ActorRunRequest runRequest = new ActorRunRequest(url);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode requestNode = mapper.valueToTree(runRequest);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    runActorUrlAsync,
                    HttpMethod.POST,
                    new HttpEntity<>(requestNode),
                    JsonNode.class
            );
            if (responseEntity.getBody() != null){
                if (responseEntity.getBody().get("data") != null
                        && responseEntity.getBody().get("data").get("id") != null){
                    request.setRunId(responseEntity.getBody().get("data").get("id").asText());
                    request.setStatus("RUN INIT");
                }
            }
        } catch (Exception e) {
            logger.info(e.toString());
            request.setRunId(e.getMessage());
            request.setStatus("RUN INIT FAILED");
        }
        queriesRepository.save(request);
    }

    public String importCompany(JobQueryRequest jobQueryRequest) {
        TamCompaniesJobQueries request = saveQuery(jobQueryRequest);
        if (request == null){
            System.out.println("Not saved.");
            return "Not imported";
        }
        return "Imported";
    }

    public void queryCompanyAsync(TamCompaniesJobQueries jobQuery) {
        String url = buildSearchUrl(jobQuery);
        runActorASync(url, jobQuery);
    }

    private String buildSearchUrl(TamCompaniesJobQueries jobQuery) {
        StringBuilder search = new StringBuilder();
        search.append("https://www.linkedin.com/jobs/search/?f_C=")
                .append(jobQuery.getCompanyLinkedinUid());
        if (jobQuery.getPostedIn()!=null){
            search.append("&f_TPR=r");
            search.append(jobQuery.getPostedIn());
        }
        search.append("&geoId=103644278");//united states
        search.append("&keywords=").append(jobQuery.getKeysTitle().split(",")[0]);
        System.out.println("query: "+search.toString());
        return search.toString();
    }

}
