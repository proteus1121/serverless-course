package com.myorg.handlers.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.DynamoDBUserAdapter;
import com.myorg.models.User;
import lombok.SneakyThrows;

import java.util.logging.Logger;

public class CreateUserHandler implements RequestHandler<SQSEvent, Void> {
    private final Logger LOGGER = Logger.getLogger(getClass().getName());
    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        for (SQSEvent.SQSMessage sqsMessage : input.getRecords()) {
            LOGGER.info("BODY : " + sqsMessage.getBody());
            User[] userNames = getUsers(sqsMessage.getBody());
            for (User userName: userNames) {
                DynamoDBUserAdapter.getInstance().createUser(userName.getName());
            }
        }

        return null;
    }

    public User[] getUsers(String sqsMessage) throws java.io.IOException {
        return objectMapper.readValue(sqsMessage, User[].class);
    }
}
