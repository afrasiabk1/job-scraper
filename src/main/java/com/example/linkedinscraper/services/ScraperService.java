package com.example.linkedinscraper.services;

import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.example.linkedinscraper.enums.LinkedinDateEnum;
import com.example.linkedinscraper.payloads.ActorRunRequest;
import com.example.linkedinscraper.payloads.JobDataSetResponse;
import com.example.linkedinscraper.payloads.JobQueryRequest;
import com.example.linkedinscraper.repositories.TamCompaniesJobQueriesRepository;
import com.example.linkedinscraper.repositories.TamCompaniesJobsRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.linkedinscraper.config.ApifyConfig.*;
import static com.example.linkedinscraper.enums.LinkedinDateEnum.getLinkedinEnum;

@Service
public class ScraperService {

    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    private final TamCompaniesJobQueriesRepository queriesRepository;
    private final TamCompaniesJobsRepository jobsRepository;

    public ScraperService(RestTemplate restTemplate, TamCompaniesJobQueriesRepository queriesRepository, TamCompaniesJobsRepository jobsRepository) {
        this.restTemplate = restTemplate;
        this.queriesRepository = queriesRepository;
        this.jobsRepository = jobsRepository;
    }

    public void filterKeywords(List<JobDataSetResponse> jobDataSetResponses, TamCompaniesJobQueries jobQueryRequest) {
        jobDataSetResponses = jobDataSetResponses.stream().filter(job -> {
            System.out.println("Job Id: " + job.getLinkedinJobId());
            boolean match = false;
            String keyMatched = strContains(job.getTitle(), jobQueryRequest.getKeysTitle());
            String keyMisMatched = strContains(job.getTitle(), jobQueryRequest.getKeysTitle());
                if (!keyMatched.isEmpty()) {
                    System.out.println("T Keyword: " + keyMatched);
                    match = true;
                } else {
                    keyMatched = strContains(job.getDescriptionText(), jobQueryRequest.getKeysBody());
                    if (!keyMatched.isEmpty()) {
                        System.out.println("B Keyword: " + keyMatched);
                        match = true;
                    }
                }
            if (match) {

                keyMisMatched = strContains(job.getTitle(), jobQueryRequest.getKeysNot());
                if (!keyMisMatched.isEmpty()) {
                    System.out.println("^TKeyword: " + keyMisMatched);
                    match = false;
                }
            }
            if (match) {

                keyMisMatched = strContains(job.getDescriptionText(), jobQueryRequest.getKeysNot());
                if (!keyMisMatched.isEmpty()) {
                    System.out.println("^BKeyword: " + keyMisMatched);
                    match = false;
                }
            }
            if (match){
                job.setKeyMatched(keyMatched);
                job.setQuery(jobQueryRequest);
                job.setDescriptionText(job.getDescriptionText().substring(0, Math.min(job.getDescriptionText().length(), 5000)));
                jobsRepository.save(job);
            }

            if (job.getPostedAt()!=null && !job.getPostedAt().isEmpty()) {
                LocalDate date = LocalDate.parse(job.getPostedAt());
                if (jobQueryRequest.getPostedAfterDate() != null) {
                    if (date.isBefore(jobQueryRequest.getPostedAfterDate())) {
                        match = false;
                    }
                }
                if (jobQueryRequest.getPostedBeforeDate() != null) {
                        if (date.isAfter(jobQueryRequest.getPostedBeforeDate())) {
                            match = false;
                        }
                }
            }

            return match;
        }).toList();
        System.out.println("Jobs Filtered " + jobDataSetResponses.size());
        jobQueryRequest.setStatus("COMPLETE");
        queriesRepository.save(jobQueryRequest);
    }

    public static String strContains(String inputStr, String items) {
        if (items != null && !items.isEmpty()) {
            for (String item : items.split(",")) {
                if (inputStr.toLowerCase().matches(".*\\b" + item.toLowerCase() + "\\b.*")) {
                    return item;
                }
            }
        }
        return "";
    }

    public List<JobDataSetResponse> parseJobs(JsonNode jobs) {
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

    private JsonNode getJobs(String dataSetId) {
        try {
            if (dataSetId != null && !dataSetId.isEmpty()) {
                ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                        getDataSetUrl(dataSetId),
                        HttpMethod.GET,
                        new HttpEntity<>(JsonNodeFactory.instance.nullNode()),
                        JsonNode.class
                );
                return responseEntity.getBody();
            }

        } catch (Exception e) {
            logger.info(e.toString());
            return JsonNodeFactory.instance.textNode("Error" + e.getMessage());
        }
        return JsonNodeFactory.instance.textNode("Error fetching dataset.");
    }

    public TamCompaniesJobQueries saveQuery(JobQueryRequest payload) {
        TamCompaniesJobQueries queryObj = TamCompaniesJobQueries.builder()
                .tamCompanyId(payload.getTamCompanyId())
                .companyLinkedinUid(payload.getCompanyLinkedinUid())
                .status("IMPORTED")
                .build();

        if (!payload.getKeywordsInTitle().isEmpty()) {
            queryObj.setKeysTitle(payload.getKeywordsInTitle());
        }
        if (!payload.getKeywordsInBody().isEmpty()) {
            queryObj.setKeysBody(payload.getKeywordsInBody());
        }
        if (!payload.getKeywordsNotInTitleAndBody().isEmpty()) {
            queryObj.setKeysNot(payload.getKeywordsNotInTitleAndBody());
        }
        if (payload.getPostedIn() != null) {
            queryObj.setPostedIn(getLinkedinEnum(payload.getPostedIn()).name());
        }
        if (!payload.getPostedBeforeDate().isEmpty()) {
            queryObj.setPostedBeforeDate(LocalDate.parse(payload.getPostedBeforeDate()));
        }

        if (!payload.getPostedAfterDate().isEmpty()) {
            queryObj.setPostedAfterDate(LocalDate.parse(payload.getPostedAfterDate()));
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
            if (responseEntity.getBody() != null) {
                if (responseEntity.getBody().get("data") != null
                        && responseEntity.getBody().get("data").get("id") != null) {
                    request.setRunId(responseEntity.getBody().get("data").get("id").asText());
                    request.setDataSetId(responseEntity.getBody().get("data").get("defaultDatasetId").asText());
                    request.setStatus("RUN_INIT");
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
        if (request == null) {
            System.out.println("Not saved.");
            return "Not imported";
        }
        return "Imported";
    }

    public void runCompanyAsync(TamCompaniesJobQueries jobQuery) {
        String url = buildSearchUrl(jobQuery);
        runActorASync(url, jobQuery);
    }

    private String buildSearchUrl(TamCompaniesJobQueries jobQuery) {
        StringBuilder search = new StringBuilder();
        search.append("https://www.linkedin.com/jobs/search/?f_C=")
                .append(jobQuery.getCompanyLinkedinUid());
        if (jobQuery.getPostedIn() != null) {
            search.append("&f_TPR=r");
            search.append(jobQuery.getPostedIn());
        }
        search.append("&geoId=103644278");//united states
        search.append("&keywords=");
        for (String key : jobQuery.getKeysTitle().split(",")){
            search.append(key).append("%20OR%20");
        }
        System.out.println("query: " + search.toString());
        return search.toString();
    }

    public void saveJobReponses(TamCompaniesJobQueries query) {
        JsonNode jobsJson = getJobs(query.getDataSetId());
        List<JobDataSetResponse> jobsList = parseJobs(jobsJson);
        filterKeywords(jobsList, query);
    }

    public String importCompanies(MultipartFile file) {
        try {
        Reader reader = new InputStreamReader(file.getInputStream());
            List<JobQueryRequest> queries = new CsvToBeanBuilder(reader)
                    .withSkipLines(1)
                    .withType(JobQueryRequest.class)
                    .build()
                    .parse();
            queries.forEach(this::saveQuery);
        }
        catch (Exception e){
            return e.getMessage();
        }
        return "Imported";
    }
}
