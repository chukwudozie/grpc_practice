package com.emeka.greet.server;

import com.emeka.greet.serviceimpl.CalculateServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import jdk.jfr.Category;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Sum Server ... loading ...");
        Locale En = new Locale("en", "IN");
       Locale EN = En.stripExtensions();
        System.out.println(En.toLanguageTag());
//        En = Locale.getDefault();

        System.out.println("Rs."+ NumberFormat.getCurrencyInstance(En).format(50.00));

        Server sumServer = ServerBuilder.forPort(50052)
                .addService(new CalculateServiceImpl())
                .addService(ProtoReflectionService.newInstance()) // Used to enable gRPC reflection
                .build();
        sumServer.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Shutdown Request Received");
            sumServer.shutdown();
            System.out.println("Server successfully stopped");
        }));

        sumServer.awaitTermination();
    }
}
