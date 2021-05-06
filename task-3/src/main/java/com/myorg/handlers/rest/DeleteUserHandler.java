package com.myorg.handlers.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.myorg.DynamoDBUserAdapter;
import lombok.SneakyThrows;

import java.util.logging.Logger;

public class DeleteUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	@SneakyThrows
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		String id = input.getPathParameters().get("id");
		logger.info("Deleting user by id: " + id);
		DynamoDBUserAdapter.getInstance().delete(id);

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setStatusCode(200);
		return responseEvent;
	}
}
