package com.emeka.greet.client;

import com.proto.prime.PrimeCalculatorGrpc;
import com.proto.prime.PrimeCalculatorRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PrimeCalculatorClient {
    public static void main(String[] args) {
        System.out.println("Starting prime Number client ... ");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",50053)
                .usePlaintext().build();

        System.out.println("Creating stub ...");
        PrimeCalculatorGrpc.PrimeCalculatorBlockingStub calculatorClient = PrimeCalculatorGrpc
                .newBlockingStub(channel);

        PrimeCalculatorRequest request = PrimeCalculatorRequest
                .newBuilder().setPrimeNumber(120)
                .build();
        calculatorClient.calculate(request)
                .forEachRemaining(primeCalculatorResponse -> {
            System.out.println(primeCalculatorResponse.getResult());
        });

        System.out.println("Shutting down client ... ");
        channel.shutdown();
    }
}
