package com.emeka.greet.client;


import com.proto.calculate.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    public static void main(String[] args) {
        System.out.println("Sum Client loading ...");
        CalculatorClient client = new CalculatorClient();
        client.run();
    }

    private void run (){
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",50052)
                .usePlaintext()
                .build();
//        doUnaryCall(channel);
//        doClientStreamingCall(channel);
//        doBIDIStreamingCall(channel);
        doErrorCall(channel);
        System.out.println("Shutting down channel...");
        channel.shutdown();
    }

    private void doUnaryCall( ManagedChannel channel){

        System.out.println("Creating the client stub ...");
CalculatorServiceGrpc.CalculatorServiceBlockingStub sumClient = CalculatorServiceGrpc.newBlockingStub(channel);

        Sum testSum = Sum.newBuilder()
                .setFirstNumber(3)
                .setSecondNumber(10)
                .build();

        SumRequest request = SumRequest.newBuilder()
                .setSum(testSum)
                .build();

        SumResponse response = sumClient.sum(request);
        System.out.println(response.getSum());

    }

    private  void  doClientStreamingCall(ManagedChannel channel){

        System.out.println("Creating stub");

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver =
                asyncClient.computeAverage(new StreamObserver<>() {
                    @Override
                    public void onNext(ComputeAverageResponse value) {
                        // We get response from server once
                        System.out.print("Received maximum from server :");
                        System.out.println(value.getAverage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t.getLocalizedMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Server response sent successfully");
                        latch.countDown();
                    }
                });

         for(int i = 1; i <= 10000; i++){
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                        .setSum(Sum.newBuilder().setFirstNumber(i)
                                .build())
                        .build());
        }


        // This tells the server that the client is done sending the requests
        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void doBIDIStreamingCall(ManagedChannel channel){
        System.out.println("Creating stub");
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

       StreamObserver<FindMaximumRequest> requestObserver =
               asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Received Response from server");
                System.out.println(value.getMaximum());

            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getLocalizedMessage());
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                System.out.println("Server done with sending messages");
            }
        });

        Arrays.asList(3, 5, 7,9, 19, 8, 13, 14).forEach(
                number ->{
                    System.out.println("Sending number: "+number);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                            .setNumber(number).build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                );
        requestObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void doErrorCall (ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub =
                CalculatorServiceGrpc.newBlockingStub(channel);
        try {
            SquareRootResponse response = blockingStub.squareRoot(SquareRootRequest.newBuilder().setNumber(-1).build());
        } catch (StatusRuntimeException e){
            System.err.println("Got an Exception");
            e.printStackTrace();
        }

    }
}
