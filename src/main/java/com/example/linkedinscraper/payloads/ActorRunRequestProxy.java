package com.example.linkedinscraper.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActorRunRequestProxy {
    public boolean useApifyProxy = true;
    public ArrayList<String> apifyProxyGroups = new ArrayList<>(List.of("RESIDENTIAL"));
    public String apifyProxyCountry = "US";
}
