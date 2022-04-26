package com.emeka.greet.serviceimpl;


import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {


    @Override
    // for unary
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        // Extract needed fields from request
       Greeting greeting =  request.getGreeting();
       String firstName = greeting.getFirstName();
       String lastName = greeting.getLastName();
       // Create the response
       String result = "Hello "+firstName+ " "+ lastName+ "!!!";
       GreetResponse response = GreetResponse.newBuilder()
               .setResult(result)
               .build();
       // The line below sends the response straight to the client
       responseObserver.onNext(response);
       // Complete the RPC call
       responseObserver.onCompleted();
    }

    @Override
    // for server stream api
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();
        String lastName = request.getGreeting().getLastName();
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + " : " + lastName + "!!!. Response Number: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder().setResult(result).build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        }catch (InterruptedException e){
                e.printStackTrace();
            } finally {
                responseObserver.onCompleted();
            }
        }

    @Override
//    for client stream api
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> requestObserver = new StreamObserver<>() {
            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                // Here client sends a message
                result += "Hello " + value.getGreeting().getFirstName() + "!";
            }

            @Override
            public void onError(Throwable t) {
                // Here client sends an error
            }
            @Override
            public void onCompleted() {
                // Here client is done sending the stream of requests and response will be returned
                responseObserver.onNext(LongGreetResponse.newBuilder()
                        .setResult(result)
                        .build());
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    @Override
//    for bidi stream api
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestObserver =  new StreamObserver<GreetEveryoneRequest>() {
                    @Override
                    public void onNext(GreetEveryoneRequest value) {
                        String result = "Hello "+value.getGreeting().getFirstName()+ "!!!";
                        GreetEveryoneResponse response =
                                GreetEveryoneResponse.newBuilder().setResult(result).build();
                        responseObserver.onNext(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };

        return requestObserver;
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {
        Context current = Context.current();
        try {
        for(int i = 0; i < 3; i++){
            if(!current.isCancelled()) {
                System.out.println("Sleep for 100 ms.");
                Thread.sleep(100);
            } else{
                return;
            }
        }
            System.out.println("Send response");
        responseObserver.onNext(
                GreetWithDeadlineResponse.newBuilder()
                        .setResult("Hello "+ request.getGreeting().getFirstName()+"!!!")
                        .build()
        );
        responseObserver.onCompleted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}

