package com.akatia.printmgr.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintJobRequest {

    @JsonProperty(value = "description", required = true)
    String description;

    @JsonProperty(value = "fileUrl", required = true)
    String fileUrl;

    @JsonProperty(value = "printerName")
    String printerName;

    @JsonProperty(value = "numOfCopies")
    Integer numOfCopies;

    @JsonProperty(value = "tray")
    Integer tray;

    @JsonProperty(value = "sides")
    Integer sides;
}
