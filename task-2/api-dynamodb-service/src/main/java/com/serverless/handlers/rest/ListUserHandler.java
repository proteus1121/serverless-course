package com.serverless.handlers.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.DynamoDBUserAdapter;
import com.serverless.models.User;
import lombok.SneakyThrows;

import java.util.List;
import java.util.logging.Logger;

public class ListUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	@SneakyThrows
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		logger.info("Fetching all users");
		List<User> users = DynamoDBUserAdapter.getInstance().userList();

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setStatusCode(200);
		responseEvent.setBody(new ObjectMapper().writeValueAsString(users));
		return responseEvent;
	}
}
