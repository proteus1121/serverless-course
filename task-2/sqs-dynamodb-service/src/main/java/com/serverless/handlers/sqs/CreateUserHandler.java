package com.serverless.handlers.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.DynamoDBUserAdapter;
import com.serverless.models.User;
import lombok.SneakyThrows;

public class CreateUserHandler implements RequestHandler<SQSEvent, Void> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public Void handleRequest(SQSEvent input, Context context) {
        for (SQSEvent.SQSMessage sqsMessage : input.getRecords()) {
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
