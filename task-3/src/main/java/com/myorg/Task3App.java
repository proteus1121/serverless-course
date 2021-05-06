package com.myorg;

import software.amazon.awscdk.core.App;

import java.util.Arrays;

public class Task3App {
    public static void main(final String[] args) {
        App app = new App();

        new Task3Stack(app, "Task3Stack");

        app.synth();
    }
}
