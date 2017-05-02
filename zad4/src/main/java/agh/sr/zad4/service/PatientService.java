package agh.sr.zad4.service;

import agh.sr.zad4.proto.PatientId;
import agh.sr.zad4.proto.PatientRecord;
import agh.sr.zad4.proto.PatientSericeGrpc;
import agh.sr.zad4.proto.ServerResponse;
import io.grpc.stub.StreamObserver;

public class PatientService extends PatientSericeGrpc.PatientSericeImplBase {

    @Override
    public void findMyRecord(PatientId request, StreamObserver<ServerResponse> responseObserver) {
        responseObserver.onNext(findSpecificPatientAndPrepareResponse(request.getId()));
        responseObserver.onCompleted();
    }

    private ServerResponse findSpecificPatientAndPrepareResponse(int id){
        if(!PseudoDatabase.CONNECTION.getPatientRecordBase().containsKey(id)){
            return ServerResponseTools.prepareNotFoundMsg("Patient isn't in database.");
        }
        else{
            PatientRecord pr = PseudoDatabase.CONNECTION.getPatientRecordBase().get(id);
            return ServerResponseTools.prepareOkServerResponseWithPatientRecord(pr, "Found in database");
        }
    }
}
