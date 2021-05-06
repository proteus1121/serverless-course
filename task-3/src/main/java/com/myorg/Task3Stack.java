package com.myorg;

import com.google.common.collect.ImmutableMap;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSource;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSourceProps;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.NotificationKeyFilter;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.Collections;
import java.util.Map;

public class Task3Stack extends Stack {
    public static final String TABLE_NAME = "Users";
    public static final String USER_QUEUE_NAME = "userFiles";

    public Task3Stack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public Task3Stack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Table table = new Table(this, TABLE_NAME, TableProps.builder()
                .partitionKey(Attribute.builder()
                        .name("id")
                        .type(AttributeType.STRING)
                        .build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());

        Queue queue = new Queue(this, USER_QUEUE_NAME);

        Bucket s3Bucket = new Bucket(this, "aishchenko-test");

        Map<String, String> environment = ImmutableMap.of("USERS_TABLE_NAME", table.getTableName(),
                "SQS_QUEUE_URL", queue.getQueueUrl());

        Function createUserFunction = new Function(this, "createUser",
                getLambdaFunctionProps(environment, "com.myorg.handlers.rest.CreateUserHandler"));
        Function deleteUserFunction = new Function(this, "deleteUser",
                getLambdaFunctionProps(environment, "com.myorg.handlers.rest.DeleteUserHandler"));
        Function getUserFunction = new Function(this, "getUser",
                getLambdaFunctionProps(environment, "com.myorg.handlers.rest.GetUserHandler"));
        Function listUserFunction = new Function(this, "listUser",
                getLambdaFunctionProps(environment, "com.myorg.handlers.rest.ListUserHandler"));

        Function s3Function = new Function(this, "s3User",
                getLambdaFunctionProps(environment, "com.myorg.handlers.s3.S3EventHandler"));

        Function sqsFunction = new Function(this, "sqsUser",
                getLambdaFunctionProps(environment, "com.myorg.handlers.sqs.CreateUserHandler"));

        table.grantFullAccess(getUserFunction);
        table.grantFullAccess(listUserFunction);
        table.grantFullAccess(createUserFunction);
        table.grantFullAccess(deleteUserFunction);

        RestApi api = new RestApi(this, "usersApi",
                RestApiProps.builder().restApiName("Users Service").build());

        IResource items = api.getRoot().addResource("users");

        Integration getAllIntegration = new LambdaIntegration(listUserFunction);
        items.addMethod("GET", getAllIntegration);

        Integration createOneIntegration = new LambdaIntegration(createUserFunction);
        items.addMethod("POST", createOneIntegration);

        IResource singleItem = items.addResource("{id}");
        Integration getOneIntegration = new LambdaIntegration(getUserFunction);
        singleItem.addMethod("GET",getOneIntegration);

        Integration deleteOneIntegration = new LambdaIntegration(deleteUserFunction);
        singleItem.addMethod("DELETE",deleteOneIntegration);

        Function fromS3ToSqsFunction = new Function(this,
                "jsonFromS3ToSqs",
                getLambdaFunctionProps(environment, "com.myorg.handlers.s3.S3EventHandler"));

        fromS3ToSqsFunction.addEventSource(new S3EventSource(s3Bucket,
                S3EventSourceProps.builder()
                        .events(Collections.singletonList(EventType.OBJECT_CREATED))
                        .filters(Collections.singletonList(NotificationKeyFilter.builder()
                                .suffix(".json")
                                .build()))
                        .build()));
        s3Bucket.grantRead(fromS3ToSqsFunction);
        queue.grantSendMessages(fromS3ToSqsFunction);

        Function sqsToDynamoDb = new Function(this,
                "jsonFromSqsToDynamo",
                getLambdaFunctionProps(environment, "com.myorg.handlers.sqs.CreateUserHandler"));
        sqsToDynamoDb.addEventSource(new SqsEventSource(queue));
        table.grantReadWriteData(sqsToDynamoDb);
        queue.grantConsumeMessages(sqsToDynamoDb);
    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> environment, String handler) {
        return FunctionProps.builder()
                .code(Code.fromAsset("target/task-3-0.1.jar"))
                .handler(handler)
                .runtime(Runtime.JAVA_8)
                .environment(environment)
                .timeout(Duration.seconds(30))
                .memorySize(1024)
                .build();
    }
}
