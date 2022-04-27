package io.softwarestrategies.printmgr.service;

import io.softwarestrategies.printmgr.data.PrintJobRequest;
import io.softwarestrategies.printmgr.data.PrintJobsRequest;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrintJobsRequestListener {

    private final ObjectMapper objectMapper;
    private final PrintService printService;

    public PrintJobsRequestListener(ObjectMapper objectMapper, PrintService printService) {
        this.objectMapper = objectMapper;
        this.printService = printService;
    }

    @SqsListener(value = "${aws.queue.name}",deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String messageAsJson) {
        PrintJobsRequest printJobsRequest = null;
        String descriptionOfCurrentPrintJobRequest = null;

        try {
            printJobsRequest = objectMapper.readValue(messageAsJson, PrintJobsRequest.class);

            for (PrintJobRequest printJobRequest : printJobsRequest.getPrintJobs()) {
                try {
                    descriptionOfCurrentPrintJobRequest = printJobRequest.getDescription();

                    if (StringUtils.isNullOrEmpty(printJobRequest.getDescription())) {
                        throw new Exception("Missing message field [description]");
                    }
                    else if (StringUtils.isNullOrEmpty(printJobRequest.getFileUrl())) {
                        throw new Exception("Missing message field [fileUrl]");
                    }

                    printService.printDocument(printJobRequest);
                }
                catch (Exception e) {
                    if (StringUtils.isNullOrEmpty(descriptionOfCurrentPrintJobRequest)) {
                        log.error("Print Job Failed: {}", e.getMessage());
                        log.debug("Exception: ", e);
                    }
                    else {
                        log.error("Print Job Failed: {} -- {}", descriptionOfCurrentPrintJobRequest, e.getMessage());
                        log.debug("Exception: ", e);
                    }
                }
            }
        }
        catch (JsonProcessingException jsonException) {
            log.error("Print Job Failed: {}", jsonException.getOriginalMessage());
            log.debug("Exception: ", jsonException);
        }
        catch (Exception e) {
            log.error("Print Job Failed: {}", e.getMessage());
            log.debug("Exception: ", e);
        }
    }
}