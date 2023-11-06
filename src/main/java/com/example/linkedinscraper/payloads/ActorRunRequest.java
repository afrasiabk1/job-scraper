package com.example.linkedinscraper.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActorRunRequest {
    public int count = 20;
    public int maxDelay = 5;
    public int minDelay = 1;
    public ActorRunRequestProxy proxy = new ActorRunRequestProxy();
    public boolean scrapeSkills = false;
    public String searchUrl;
    public int startPage = 1;

    public ActorRunRequest(String searchUrl) {
        this.searchUrl = searchUrl;
    }
}


