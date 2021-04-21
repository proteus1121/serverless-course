package com.serverless.handlers.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.logging.Logger;

public class S3EventHandler implements RequestHandler<S3Event, String> {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getUrlDecodedKey();

        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        String fireContent = s3Client.getObjectAsString(srcBucket, srcKey);
        logger.info("Received file " + srcKey);

        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(System.getenv("SQS_QUEUE_URL"))
                .withMessageBody("File: " + srcKey + System.lineSeparator() + "File content: " + fireContent)
                .withDelaySeconds(1);

        sqs.sendMessage(sendMsgRequest);
        return "";
    }
}
