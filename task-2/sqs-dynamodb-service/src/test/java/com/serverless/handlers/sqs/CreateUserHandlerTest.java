package com.serverless.handlers.sqs;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateUserHandlerTest {

    CreateUserHandler user = new CreateUserHandler();

    @Test
    public void getUsers() throws IOException {
        String s = new String(Files.readAllBytes(Paths.get("/Users/aishchenko/Documents/serverless/task-2/sqs-dynamodb-service/src/test/resources/users.json")));
        user.getUsers(s);
    }
}
