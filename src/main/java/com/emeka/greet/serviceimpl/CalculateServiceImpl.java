package com.emeka.greet.serviceimpl;

import com.proto.calculate.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculateServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {


    //Unary API service
    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        // Extract the needed fields from request
        // Method 1
//        Sum sum = request.getSum();
//        int first = sum.getFirstNumber();
//        int second = sum.getSecondNumber();
//        int result = first + second;
//
//        // Create the response
//        SumResponse response = SumResponse.newBuilder()
//                .setSum(result)
//                .build();

//        method 2
        SumResponse response = SumResponse.newBuilder()
                        .setSum(request.getSum().getFirstNumber() + request.getSum().getSecondNumber())
                                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
//        super.summation(request, responseObserver);
    }

    // Client streaming API service
    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {

        StreamObserver<ComputeAverageRequest> requestObserver = new StreamObserver<>() {
            int sum = 0;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest value) {
                sum  += value.getSum().getFirstNumber();
                count += 1;
            }

            @Override
            public void onError(Throwable t) {
                // Error from client request
                System.out.println(t.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ComputeAverageResponse
                        .newBuilder().setAverage((double) sum/count).build());
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    // BIDI streaming API service
    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        StreamObserver<FindMaximumRequest> requestObserver = new StreamObserver<FindMaximumRequest>() {
            int currentMaximum = 0;
            @Override
            public void onNext(FindMaximumRequest value) {
                int number = value.getNumber();
                if (number > currentMaximum) currentMaximum = number;
                FindMaximumResponse response = FindMaximumResponse
                        .newBuilder().setMaximum(currentMaximum).build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // Send the current last maximum
                responseObserver.onNext(FindMaximumResponse.newBuilder()
                        .setMaximum(currentMaximum).build());
                // the server is done sending data
                responseObserver.onCompleted();

            }
        };

        return requestObserver;
    }

    /**
     *Implementation of a unary API that calculates the square root of a number
     * but throws an exception when the incoming request is a negative number
     */
    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
       int number =  request.getNumber();

       if(number >= 0) {
           double sqrt = Math.sqrt(number);
       responseObserver.onNext(SquareRootResponse.newBuilder().setSquareRoot(sqrt).build());
       responseObserver.onCompleted();
       } else {
//           construct the exception
           responseObserver.onError(
                   Status.INVALID_ARGUMENT
                           .withDescription("The number being sent is not positive")
                           .augmentDescription("Please change the number "+number+ " to a positive number")
                           .asRuntimeException()
           );
       }
       responseObserver.onCompleted();
    }


}
