package agh.sr.zad4.service;


import agh.sr.zad4.proto.PatientRecord;
import agh.sr.zad4.proto.ServerResponse;
import agh.sr.zad4.proto.TechServiceGrpc;
import agh.sr.zad4.proto.TestResult;
import io.grpc.stub.StreamObserver;

public class TechService extends TechServiceGrpc.TechServiceImplBase{

    @Override
    public void addResultToSystem(TestResult request, StreamObserver<ServerResponse> responseObserver) {
        if(!PseudoDatabase.CONNECTION.getPatientRecordBase().containsKey(request.getPatientId())){
            responseObserver.onNext(
                    ServerResponseTools.prepareNotFoundMsg("Patient isn't in database.")
            );
        }
        else{
            responseObserver.onNext(
                    ServerResponseTools.prepareCreatedServerResponse()
            );
        }
        responseObserver.onCompleted();
    }

}
