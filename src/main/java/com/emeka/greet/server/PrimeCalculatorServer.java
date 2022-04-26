package com.emeka.greet.server;

import com.emeka.greet.serviceimpl.PrimeCalculatorServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class PrimeCalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Prime Calculator server Running .... ");

        Server server = ServerBuilder.forPort(50053)
                .addService(new PrimeCalculatorServiceImpl())
                .build();

        // Boilerplate code for starting and stopping a server
        server.start();
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Shutdown Request Received");
            server.shutdown();
            System.out.println("Server successfully stopped");
        }));
        server.awaitTermination();
    }
}
