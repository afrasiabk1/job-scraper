package com.example.linkedinscraper.payloads;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

public class JobDataSetResponse {
    private String id;
    private String link;
    private String title;
    private String companyName;
    private String companyLinkedinUrl;
    private String postedAt;
    private String descriptionText;
}
