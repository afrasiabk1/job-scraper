package com.example.linkedinscraper.payloads;

import com.example.linkedinscraper.enums.LinkedinDateEnum;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobQueryRequest {

    private String tamCompanyId;
    private String companyLinkedinUid;
    private ArrayList<String> keywordsInTitle = new ArrayList<>();
    private ArrayList<String> keywordsInBody = new ArrayList<>();
    private ArrayList<String> keywordsNotInTitleAndBody = new ArrayList<>();
    private LocalDate postedAfterDate;
    private LocalDate postedBeforeDate;
    private LinkedinDateEnum postedIn;

}
