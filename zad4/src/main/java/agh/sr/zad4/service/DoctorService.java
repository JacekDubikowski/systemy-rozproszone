package agh.sr.zad4.service;

import agh.sr.zad4.proto.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DoctorService extends DoctorServiceGrpc.DoctorServiceImplBase{

    @Override
    public void findRecords(QueryParams request, StreamObserver<ServerResponse> responseObserver) {
        if(request.getPatientId()!=0){
            Optional<PatientRecord> pr = PseudoDatabase.CONNECTION.findParticularPatientRecord(request.getPatientId());
            if(pr.isPresent()){
                if(request.getParamsList().size()==0) {
                    responseObserver.onNext(ServerResponse
                            .newBuilder()
                            .setCode(ServerResponse.ServerResponseCode.OK)
                            .setMsg("Found in database")
                            .setRecord(pr.get())
                            .build()
                    );
                }
                else{
                    PatientRecord patientRecord = pr.get();
                    List<TestResult> foundResults = findInterestingTestResults(request, patientRecord);
                    if(foundResults.size()!=0) responseObserver.onNext(prepareServerResponse(patientRecord, foundResults));
                    else{
                        responseObserver.onNext(ServerResponse
                                .newBuilder()
                                .setCode(ServerResponse.ServerResponseCode.NOT_FOUND)
                                .setMsg("No such result for patient in database.")
                                .build()
                        );
                    }
                }
            }
            else{
                responseObserver.onNext(PseudoDatabase.prepareNotFoundMsg());
            }
        }
        else {
            PseudoDatabase.CONNECTION.getPatientRecordBase().values().forEach(patientRecord -> {
                List<TestResult> foundResults = findInterestingTestResults(request, patientRecord);
                if(foundResults.size()!=0) responseObserver.onNext(prepareServerResponse(patientRecord, foundResults));
            });
        }
        responseObserver.onCompleted();
    }

    private List<TestResult> findInterestingTestResults(QueryParams request, PatientRecord patientRecord) {
        List<TestResult> foundResults = new LinkedList<>();
        patientRecord.getResultsList().forEach(testResult -> {
            List<Parameter> found = new ArrayList<>();
            request.getParamsList().forEach(toCompare -> {
                found.addAll(collectMatching(testResult, toCompare)
                );
            });
            if(found.size()==request.getParamsList().size()) foundResults.add(testResult.toBuilder().clearParameters().addAllParameters(found).build());
        });
        return foundResults;
    }

    private List<Parameter> collectMatching(TestResult testResult, Parameter toCompare) {
        return testResult
                .getParametersList()
                .stream()
                .filter(p -> compareParam(p,toCompare))
                .collect(Collectors.toList());
    }

    private ServerResponse prepareServerResponse(PatientRecord patientRecord, List<TestResult> foundResults) {
        return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.OK)
                .setMsg("Found in database")
                .setRecord(patientRecord.toBuilder().clearResults().addAllResults(foundResults).build())
                .build();
    }

    private boolean compareParam(Parameter paramCurrent, Parameter toCompare){
        return paramCurrent.getName().equals(toCompare.getName()) && paramCurrent.getValue() >= toCompare.getMin() && paramCurrent.getValue() <= toCompare.getMax();
    }


}
