package com.example.linkedinscraper.services;
import com.example.linkedinscraper.payloads.ActorRunRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ScraperService {

    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(ScraperService.class);
    public ScraperService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<JsonNode> ping() {
        String runActorUrl = "https://api.apify.com/v2/acts/gdbRh93zn42kBYDyS/runs?token=apify_api_RxR8cb15J5Wiz9otZBnHfGdGK5a6J33xGteb&memory=512";
        String searchUrl = "https://www.linkedin.com/jobs/search/?f_C=1586&keywords=software";
        ActorRunRequest runRequest = new ActorRunRequest(searchUrl);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode requestNode = mapper.valueToTree(runRequest);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    runActorUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestNode),
                    JsonNode.class
            );
            logger.info("Response: \n"+ responseEntity);
            return responseEntity;
        }
        catch (Exception e){
            logger.info(e.toString());
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }
}
