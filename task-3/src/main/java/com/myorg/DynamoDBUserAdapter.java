package com.myorg;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.myorg.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBUserAdapter {
    private static DynamoDBUserAdapter INSTANCE;

    public static DynamoDBUserAdapter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DynamoDBUserAdapter();

        return INSTANCE;
    }

    private final DynamoDBMapper mapper;

    private DynamoDBUserAdapter() {
        String tableName = System.getenv("USERS_TABLE_NAME");
        AmazonDynamoDB database = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(tableName))
                .build();
        this.mapper = new DynamoDBMapper(database, mapperConfig);
    }

    public void createUser(String userName) {
        User user = new User(userName);
        this.mapper.save(user);
    }

    public User get(String id) {
        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<User> queryExp = new DynamoDBQueryExpression<User>()
                .withKeyConditionExpression("id = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<User> result = this.mapper.query(User.class, queryExp);
        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public void delete(String id) {
        User user = get(id);
        if (user != null) {
            this.mapper.delete(user);
        }
    }

    public List<User> userList() {
        DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
        return this.mapper.scan(User.class, scanExp);
    }
}
