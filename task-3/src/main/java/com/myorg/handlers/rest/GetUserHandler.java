package com.myorg.handlers.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.DynamoDBUserAdapter;
import com.myorg.models.User;
import lombok.SneakyThrows;

import java.util.logging.Logger;

public class GetUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private Logger logger = Logger.getLogger(getClass().getName());

    @SneakyThrows
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String id = input.getPathParameters().get("id");
        logger.info("Fetching user by id: " + id);
        User user = DynamoDBUserAdapter.getInstance().get(id);

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(200);
        responseEvent.setBody(new ObjectMapper().writeValueAsString(user));
        return responseEvent;
    }
}
