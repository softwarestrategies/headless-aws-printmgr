package io.softwarestrategies.printmgr.service;

import io.softwarestrategies.printmgr.data.PrintJobRequest;
import io.softwarestrategies.printmgr.data.PrintJobsRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class PrintJobsRequestListenerTest {

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.10.0"))
                    .withServices(SQS)
                    .withEnv("DEFAULT_REGION", "eu-central-1");

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @TestConfiguration
    @ActiveProfiles("test")
    static class AwsTestConfig {

        @Bean
        public QueueMessagingTemplate queueMessagingTemplate() {
            return new QueueMessagingTemplate(amazonSQS());
        }

        @Bean
        public AmazonS3 amazonS3() {
            return AmazonS3ClientBuilder.standard()
                    .withCredentials(localStack.getDefaultCredentialsProvider())
                    .withEndpointConfiguration(localStack.getEndpointConfiguration(S3))
                    .build();
        }

        @Bean
        public AmazonSQSAsync amazonSQS() {
            return AmazonSQSAsyncClientBuilder.standard()
                    .withCredentials(localStack.getDefaultCredentialsProvider())
                    .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                    .build();
        }
    }

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String QUEUE_NAME = "akatia_printmgr_client1_instance1";
    private static final String BUCKET_NAME = "order-event-test-bucket";

    @Ignore
    @Test
    public void testMessageShouldBeUploadedToS3OnceConsumed() throws JsonProcessingException {
        PrintJobsRequest printJobsRequest = PrintJobsRequest.builder()
                .printJobs(Arrays.asList(
                        PrintJobRequest.builder().description("test1").fileUrl("url1").build(),
                        PrintJobRequest.builder().description("test2").fileUrl("url2").printerName("EPSON WF-3730 Series Test Override").build()
                ))
                .build();

        queueMessagingTemplate.convertAndSend(QUEUE_NAME, objectMapper.writeValueAsString(printJobsRequest));
    }
}
