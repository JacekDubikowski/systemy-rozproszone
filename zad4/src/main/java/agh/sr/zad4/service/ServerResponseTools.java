package agh.sr.zad4.service;

import agh.sr.zad4.proto.PatientRecord;
import agh.sr.zad4.proto.ServerResponse;
import agh.sr.zad4.proto.TestResult;

import java.util.List;

public class ServerResponseTools {

    static ServerResponse prepareCreatedServerResponse() {
        return ServerResponse
        .newBuilder()
        .setCode(ServerResponse.ServerResponseCode.CREATED)
        .build();
    }

    public static ServerResponse prepareNotFoundMsg(String msg){
        return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.NOT_FOUND)
                .setMsg(msg)
                .build();
    }

    static ServerResponse prepareOkServerResponseWithPatientRecord(PatientRecord pr, String msg) {
        return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.OK)
                .setMsg(msg)
                .setRecord(pr)
                .build();
    }

    static ServerResponse prepareServerResponseForProvidedPatientRecordFilledWithGivenResults(PatientRecord patientRecord, List<TestResult> foundResults) {
        return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.OK)
                .setMsg("Found in database")
                .setRecord(patientRecord.toBuilder().clearResults().addAllResults(foundResults).build())
                .build();
    }
}
