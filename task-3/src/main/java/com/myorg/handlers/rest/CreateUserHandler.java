package com.myorg.handlers.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.DynamoDBUserAdapter;
import lombok.SneakyThrows;

import java.util.logging.Logger;

public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{

	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	@SneakyThrows
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		JsonNode body = new ObjectMapper().readTree(input.getBody());
		String user = body.get("user").asText();
		logger.info("Creating user: " + user);
		DynamoDBUserAdapter.getInstance().createUser(user);

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setStatusCode(201);
		return responseEvent;
	}
}
