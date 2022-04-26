package com.emeka.greet.serviceimpl;

import com.proto.greet.GreetManyTimesResponse;
import com.proto.prime.PrimeCalculatorGrpc;
import com.proto.prime.PrimeCalculatorRequest;
import com.proto.prime.PrimeCalculatorResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class PrimeCalculatorServiceImpl extends PrimeCalculatorGrpc.PrimeCalculatorImplBase {
    @Override
    public void calculate(PrimeCalculatorRequest request, StreamObserver<PrimeCalculatorResponse> responseObserver) {
        // 1. My Solution
//        System.out.println("I got here");
//        int number = request.getPrimeNumber();
//        List<Integer> output = calculatePrime(number);
//        try {
//        for(int i = 0; i < output.size(); i++){
//            int result = calculatePrime(number).get(i);
//            PrimeCalculatorResponse response = PrimeCalculatorResponse
//                    .newBuilder().setResult(result).build();
//            responseObserver.onNext(response);
//            Thread.sleep(1000L);
//            }
//        } catch (InterruptedException e){
//            e.printStackTrace();;
//        } finally {
//            responseObserver.onCompleted();
//        }
        //2.    Solution from video.
        int number = request.getPrimeNumber();
        int divisor = 2;
        while(number > 1){
            if(number % divisor == 0){
                number = number/divisor;
                responseObserver.onNext(PrimeCalculatorResponse.newBuilder()
                        .setResult(divisor).build());
            } else divisor += 1;
        }
        responseObserver.onCompleted();

    }

    // helper method
//    public static List<Integer>  calculatePrime (int number){
//        List<Integer> numbers = new ArrayList<>();
//        int initial = 2;
//        while (number > 1){
//            if( (number % initial) == 0){
//                numbers.add(initial);
//                number = number /initial;
//            } else initial +=1;
//        }
//        System.out.println(numbers.size());
//        return numbers;
//    }
}
