package com.emeka.greet.server;

import com.emeka.greet.serviceimpl.GreetServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC Server");

        // Create a server with the port 50051
        // Plain text server
//        Server server = ServerBuilder
//                .forPort(50051)
//                .addService(new GreetServiceImpl())
//                .build();

        // Secure Server
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                        .useTransportSecurity(
                                new File("ssl/server.crt"),
                                new File("ssl/server.pem")
                        ).build();

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
