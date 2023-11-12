package com.example.linkedinscraper.payloads;

import com.example.linkedinscraper.enums.LinkedinDateEnum;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.opencsv.bean.CsvBindByPosition;
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

    @CsvBindByPosition(position = 0)
    private String tamCompanyId;
    @CsvBindByPosition(position = 1)
    private String companyLinkedinUid;
    @CsvBindByPosition(position = 2)
    private String keywordsInTitle;
    @CsvBindByPosition(position = 3)
    private String keywordsInBody;
    @CsvBindByPosition(position = 4)
    private String keywordsNotInTitleAndBody;
    @CsvBindByPosition(position = 5)
    private String postedIn;
    @CsvBindByPosition(position = 6)
    private String postedAfterDate;
    @CsvBindByPosition(position = 7)
    private String postedBeforeDate;

}
