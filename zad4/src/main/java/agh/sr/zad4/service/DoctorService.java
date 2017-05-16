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
            handleOnePatient(request, responseObserver);
        }
        else {
            HandleMultiplyPatient(request, responseObserver);
        }
    }

    private void HandleMultiplyPatient(QueryParams request, StreamObserver<ServerResponse> responseObserver) {
        PseudoDatabase.CONNECTION.getPatientRecordBase().values().forEach(patientRecord -> {
            List<TestResult> foundResults = findInterestingTestResults(request, patientRecord);
            if(foundResults.size()!=0){
                responseObserver.onNext(ServerResponseTools.prepareServerResponseForProvidedPatientRecordFilledWithGivenResults(patientRecord, foundResults));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        });
        responseObserver.onCompleted();
    }

    private void handleOnePatient(QueryParams request, StreamObserver<ServerResponse> responseObserver) {
        Optional<PatientRecord> pr = PseudoDatabase.CONNECTION.findParticularPatientRecord(request.getPatientId());
        if(pr.isPresent()){
            if(request.getParamsList().size()==0) {
                responseObserver.onNext(ServerResponseTools.prepareOkServerResponseWithPatientRecord(pr.get(),"Found in database"));
            }
            else{
                PatientRecord patientRecord = pr.get();
                List<TestResult> foundResults = findInterestingTestResults(request, patientRecord);
                if(foundResults.size()!=0) responseObserver.onNext(ServerResponseTools.prepareServerResponseForProvidedPatientRecordFilledWithGivenResults(patientRecord, foundResults));
                else{
                    responseObserver.onNext(ServerResponseTools.prepareNotFoundMsg("No such result for patient in database."));
                }
            }
        }
        else{
            responseObserver.onNext(ServerResponseTools.prepareNotFoundMsg("Patient isn't in database."));
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

    private boolean compareParam(Parameter paramCurrent, Parameter toCompare){
        return paramCurrent.getName().equals(toCompare.getName()) && paramCurrent.getValue() >= toCompare.getMin() && paramCurrent.getValue() <= toCompare.getMax();
    }


}
