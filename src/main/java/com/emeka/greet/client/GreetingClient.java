package com.emeka.greet.client;


import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello gRPC client");
        GreetingClient main = new GreetingClient();
        main.run();
    }
    private void run() throws SSLException {
        // Normal channel used for development
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",50051)
                .usePlaintext() // this forces SSL to be deactivated during development
                .build();

        // Channel with server authentication SSL/TLS; custom CA root certificates
        // Secured channel in production
        ManagedChannel securedChannel = NettyChannelBuilder.forAddress("localhost",50051)
                        .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt"))
                                        .build()).build();
//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreaming(channel);
//        doBIDIStreaming(channel);
//        doUnaryCallWithDeadline(channel);

        // Do unary call over a secured channel
        doUnaryCall(securedChannel);
        System.out.println("shutting down channel ");
        channel.shutdown();
    }
    private void doUnaryCall(ManagedChannel channel){
        // Create a greet service client (blocking -synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
//        GreetServiceGrpc.GreetServiceFutureStub greetClientAsync = GreetServiceGrpc.newFutureStub(channel);

        // Unary
        Greeting greeting = Greeting.newBuilder().setFirstName("Emeka")
                .setLastName("Chukwudozie").build();
// Create a greet request
        GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();
        //The greet client can directly call an rpc function and get back a response(protocol buffers)
        GreetResponse response =  greetClient.greet(request);
        System.out.println(response.getResult());
    }
    private void doServerStreamingCall(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Emeka")).build();
//        stream the responses(10) in a blocking manner for one request
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }
    private void doClientStreaming(ManagedChannel channel){
        // create a client(stub) - client must be asynchronous
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestObserver =
                asyncClient.longGreet(new StreamObserver<>() {
                    @Override
                    public void onNext(LongGreetResponse value) {
                        // we get response from server only once
                        System.out.println("Response received from server");
                        System.out.println(value.getResult());
                    }

                    @Override
                    public void onError(Throwable t) {
                        // we get error from the server

                    }

                    @Override
                    public void onCompleted() {
                        // server completed sending data, called after onNext()
                        System.out.println("Server has completed sending response");
                        latch.countDown();
                    }
                });
// 3 streaming request messages
        System.out.println("sending message 1 ...");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Emekus")
                                .build())
                        .build());
        System.out.println("Sending message 2 ...");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Peter")
                        .build())
                .build());

        System.out.println("Sending Message 3 ...");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Mark")
                        .build())
                .build());

        // This tells the server that the client is done sending the requests
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void doBIDIStreaming(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver =
                asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.print("Response from server: ");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });
        Arrays.asList("Emeka", "Ifeanyi", "Chimezie", "Tony").forEach(
                person -> {
                    System.out.println("Sending : "+person);
                    requestObserver
                        .onNext(GreetEveryoneRequest.newBuilder()
                                .setGreeting(Greeting.newBuilder().setFirstName(person)).build());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void doUnaryCallWithDeadline(ManagedChannel channel){
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call : 1000 ms deadline
        try{
            System.out.println("Sending a request with deadline of 10000 ms");
           GreetWithDeadlineResponse response =  blockingStub.withDeadline(Deadline.after(10000,TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName("Emeka").build())
                            .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceeded, response no longer needed");
            }else e.printStackTrace();
        }

        // Second call
        try{
            System.out.println("Sending a request with deadline of 100 ms");
            GreetWithDeadlineResponse response =  blockingStub.withDeadline(Deadline.after(100,TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName("Ebuka").getDefaultInstanceForType())
                            .build());
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceeded, response no longer needed");
            }else e.printStackTrace();
        }


    }
}
