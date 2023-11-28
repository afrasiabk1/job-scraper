package com.example.linkedinscraper.services;

import com.example.linkedinscraper.entities.Customers;
import com.example.linkedinscraper.entities.TamCompaniesJobQueries;
import com.example.linkedinscraper.payloads.*;
import com.example.linkedinscraper.repositories.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final CompanySignalsRepository companySignalsRepository;
    private final CustomersRepository customersRepository;
    private final MetricsRepository metricsRepository;


    public ScraperService(RestTemplate restTemplate, TamCompaniesJobQueriesRepository queriesRepository, TamCompaniesJobsRepository jobsRepository, CompanySignalsRepository companySignalsRepository, CustomersRepository customersRepository, MetricsRepository metricsRepository) {
        this.restTemplate = restTemplate;
        this.queriesRepository = queriesRepository;
        this.jobsRepository = jobsRepository;
        this.companySignalsRepository = companySignalsRepository;
        this.customersRepository = customersRepository;
        this.metricsRepository = metricsRepository;
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
            if (match) {
                job.setKeyMatched(keyMatched);
                job.setQuery(jobQueryRequest);
                job.setDescriptionText(job.getDescriptionText().substring(0, Math.min(job.getDescriptionText().length(), 5000)));
                jobsRepository.save(job);
            }

            if (job.getPostedAt() != null && !job.getPostedAt().isEmpty()) {
                LocalDate date = LocalDate.parse(job.getPostedAt());
                System.out.println("Posted at: " + date);
                if (jobQueryRequest.getPostedAfterDate() != null) {
                    if (date.isBefore(jobQueryRequest.getPostedAfterDate())) {
                        match = false;
                        System.out.println("Should be Posted after : " + jobQueryRequest.getPostedAfterDate());
                    }
                }
                if (jobQueryRequest.getPostedBeforeDate() != null) {
                    if (date.isAfter(jobQueryRequest.getPostedBeforeDate())) {
                        match = false;
                        System.out.println("Should be Posted before : " + jobQueryRequest.getPostedBeforeDate());
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
            for (String item : items.split(", ")) {
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
        if (payload.getPostedIn() != null && getLinkedinEnum(payload.getPostedIn()) != null) {
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
        for (String key : jobQuery.getKeysTitle().split(", ")) {
            search.append(key).append(" OR ");
        }
        int length = search.toString().length();
        if (length >= 4) {
            search = new StringBuilder(search.substring(0, length - 4));
        }
        System.out.println("query: S" + search.toString() + "E");
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
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Imported";
    }

    public void storeMetrics() {

        //Fetching all campaigns once
        System.out.println("Fetching All Campaigns");
        List<Campaign> campaigns = new ArrayList<>();
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    smartleadGetAllCampaigns,
                    HttpMethod.GET,
                    new HttpEntity<>(JsonNodeFactory.instance.nullNode()),
                    JsonNode.class
            );
            if (responseEntity.getBody() != null) {
                if (responseEntity.getBody() != null && responseEntity.getBody().isArray()) {
                    for (JsonNode campaign : responseEntity.getBody()) {
                        if (campaign.get("name") != null) {
                            String campName = campaign.get("name").asText();
                            if (campName.split("-").length > 0) {
                                String customerName = campName.split("-")[0];
                                if (customersRepository.findCustomersByNameAndTracked(customerName, true) != null) {
                                    if (campaign.get("id") != null && !campaign.get("id").asText().isEmpty()) {
                                        campaigns.add(Campaign.builder().campaignId(campaign.get("id").asText())
                                                .customer(customerName).build());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        List<Customers> customersList = customersRepository.findAllByTracked(true);
        customersList.forEach(c -> {

            System.out.println("Customer: "+c.getName());

            Instant instant = Instant.now();
            LocalDate dateStart = instant.atZone(ZoneId.of("America/New_York")).toLocalDate().atStartOfDay().toLocalDate();
            LocalDate dateEnd = dateStart.plusDays(1);

            Metrics m = new Metrics();

            m.setCustomer(c.getName());
            m.setCustomerId(c.getId());
                m.setDate(dateStart);
                m.setNewAccountsWithSignals(companySignalsRepository.findNewAccountsWithSignals(c.getId(), dateStart, dateEnd));
                m.setNewTamAccountsIngested(companySignalsRepository.findNewTamAccountsIngested(c.getId(), dateStart, dateEnd));
                m.setNewTamAccountsSegmented(companySignalsRepository.findNewTamAccountsSegmented(c.getId(), dateStart, dateEnd));
                m.setAccountsContacted(companySignalsRepository.findAccountsContacted(c.getId(), dateStart, dateEnd));
                m.setProspectsFound(companySignalsRepository.findProspectsFound(c.getId(), dateStart, dateEnd));
                m.setProspectsContacted(companySignalsRepository.findProspectsContacted(c.getId(), dateStart, dateEnd));
                m.setVerifiedEmailsFound(companySignalsRepository.findVerifiedEmailsFound(c.getId(), dateStart, dateEnd));
                if (!m.getProspectsContacted().equals(BigDecimal.ZERO) && m.getProspectsContacted().compareTo(m.getVerifiedEmailsFound()) > 0) {
                    m.setVerifiedEmailsFoundPc(m.getVerifiedEmailsFound().divide(m.getProspectsContacted(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                }
                if (!m.getNewTamAccountsIngested().equals(BigDecimal.ZERO) && m.getNewTamAccountsIngested().compareTo(m.getNewTamAccountsSegmented()) > 0) {
                    m.setNewTamAccountsSegmentedPc(m.getNewTamAccountsSegmented().divide(m.getNewTamAccountsIngested(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                }

                //fetching all campaigns of a customer
            m = fetchCampaignMetrics(m, campaigns.stream().filter(c0->c0.getCustomer().equals(c.getName())).toList(), dateStart);

            if (metricsRepository.findByCustomerIdAndDate(c.getId(),dateStart)!=null) {
                Metrics md = metricsRepository.findByCustomerIdAndDate(c.getId(),dateStart);
                metricsRepository.delete(md);
            }
                metricsRepository.save(m);
            System.out.println("Saved: "+m.getCustomer());
        });
    }

    private Metrics fetchCampaignMetrics(Metrics m, List<Campaign> campaigns, LocalDate date) {
        campaigns.forEach(cam -> {
            System.out.println("Campaign Id: "+cam.getCampaignId());

            try {
                ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                        getCampaignUrlByDate(cam.getCampaignId(), String.valueOf(date)),
                        HttpMethod.GET,
                        new HttpEntity<>(JsonNodeFactory.instance.nullNode()),
                        JsonNode.class
                );

                if (responseEntity.getBody() != null) {
                    JsonNode data = responseEntity.getBody();
                    if (data.get("sent_count") != null) {
                        m.setProspectsDelivered(m.getProspectsDelivered().add(BigDecimal.valueOf(data.get("sent_count").asInt())));
                    }
                    if (data.get("open_count") != null) {
                        m.setEmailsOpened(m.getEmailsOpened().add(BigDecimal.valueOf(data.get("open_count").asInt())));
                    }
                    if (data.get("reply_count") != null) {
                        m.setEmailsReplied(m.getEmailsReplied().add(BigDecimal.valueOf(data.get("reply_count").asInt())));
                    }
                    if (data.get("bounce_count") != null) {
                        m.setEmailsBounced(m.getEmailsBounced().add(BigDecimal.valueOf(data.get("bounce_count").asInt())));
                    }
                    if (!m.getProspectsDelivered().equals(BigDecimal.ZERO) && m.getProspectsDelivered().compareTo(m.getEmailsReplied()) > 0) {
                        m.setReplyRate(m.getEmailsReplied().divide(m.getProspectsDelivered(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                    }
                    if (!m.getProspectsDelivered().equals(BigDecimal.ZERO) && m.getProspectsDelivered().compareTo(m.getEmailsBounced()) > 0) {
                        m.setBounceRate(m.getEmailsBounced().divide(m.getProspectsDelivered(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                    }
                    if (!m.getEmailsOpened().equals(BigDecimal.ZERO) && m.getEmailsOpened().compareTo(m.getEmailsReplied()) > 0) {
                        m.setOpenToReplyRate(m.getEmailsReplied().divide(m.getEmailsOpened(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                    }
                    if (!m.getProspectsDelivered().equals(BigDecimal.ZERO) && m.getProspectsDelivered().compareTo(m.getEmailsOpened()) > 0) {
                        m.setOpenRate(m.getEmailsOpened().divide(m.getProspectsDelivered(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        return m;
    }

    public void storeMetricsAMonthOnce() {


        //Fetching all campaigns once
        System.out.println("Fetching All Campaigns");
        List<Campaign> campaigns = new ArrayList<>();
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    smartleadGetAllCampaigns,
                    HttpMethod.GET,
                    new HttpEntity<>(JsonNodeFactory.instance.nullNode()),
                    JsonNode.class
            );
            if (responseEntity.getBody() != null) {
                if (responseEntity.getBody() != null && responseEntity.getBody().isArray()) {
                    for (JsonNode campaign : responseEntity.getBody()) {
                        if (campaign.get("name") != null) {
                            String campName = campaign.get("name").asText();
                            if (campName.split("-").length > 0) {
                                String customerName = campName.split("-")[0];
                                if (customersRepository.findCustomersByNameAndTracked(customerName, true) != null) {
                                    if (campaign.get("id") != null && !campaign.get("id").asText().isEmpty()) {
                                        campaigns.add(Campaign.builder().campaignId(campaign.get("id").asText())
                                                .customer(customerName).build());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        List<Customers> customersList = customersRepository.findAllByTracked(true);
        customersList.forEach(c -> {
            System.out.println("Customer: " + c.getName());

            for (int i = 0; i < 30; i++ ) {

                Instant instant = Instant.now();
                LocalDate dateStart = instant.atZone(ZoneId.of("America/New_York")).toLocalDate().minusDays(i).atStartOfDay().toLocalDate();
                LocalDate dateEnd = dateStart.plusDays(1);

                System.out.println("Date: " + dateStart);

                Metrics m = new Metrics();

                m.setCustomer(c.getName());
                m.setCustomerId(c.getId());
                m.setDate(dateStart);
                m.setNewAccountsWithSignals(companySignalsRepository.findNewAccountsWithSignals(c.getId(), dateStart, dateEnd));
                m.setNewTamAccountsIngested(companySignalsRepository.findNewTamAccountsIngested(c.getId(), dateStart, dateEnd));
                m.setNewTamAccountsSegmented(companySignalsRepository.findNewTamAccountsSegmented(c.getId(), dateStart, dateEnd));
                m.setAccountsContacted(companySignalsRepository.findAccountsContacted(c.getId(), dateStart, dateEnd));
                m.setProspectsFound(companySignalsRepository.findProspectsFound(c.getId(), dateStart, dateEnd));
                m.setProspectsContacted(companySignalsRepository.findProspectsContacted(c.getId(), dateStart, dateEnd));
                m.setVerifiedEmailsFound(companySignalsRepository.findVerifiedEmailsFound(c.getId(), dateStart, dateEnd));
                if (!m.getProspectsContacted().equals(BigDecimal.ZERO) && m.getProspectsContacted().compareTo(m.getVerifiedEmailsFound()) > 0) {
                    m.setVerifiedEmailsFoundPc(m.getVerifiedEmailsFound().divide(m.getProspectsContacted(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                }
                if (!m.getNewTamAccountsIngested().equals(BigDecimal.ZERO) && m.getNewTamAccountsIngested().compareTo(m.getNewTamAccountsSegmented()) > 0) {
                    m.setNewTamAccountsSegmentedPc(m.getNewTamAccountsSegmented().divide(m.getNewTamAccountsIngested(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                }

                //fetching all campaigns of a customer
                m = fetchCampaignMetrics(m, campaigns.stream().filter(c0 -> c0.getCustomer().equals(c.getName())).toList(), dateStart);

                if (metricsRepository.findByCustomerIdAndDate(c.getId(), dateStart) != null) {
                    Metrics md = metricsRepository.findByCustomerIdAndDate(c.getId(), dateStart);
                    metricsRepository.delete(md);
                }
                metricsRepository.save(m);
                System.out.println("Saved: " + m.getCustomer());
                if (c.getName().equals("Kustomer")) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }


    public List<Metrics> getMetrics() {
        return metricsRepository.findAllSorted();
    }
}
