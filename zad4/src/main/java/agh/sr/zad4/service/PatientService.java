package agh.sr.zad4.service;

import agh.sr.zad4.proto.PatientId;
import agh.sr.zad4.proto.PatientSericeGrpc;
import agh.sr.zad4.proto.ServerResponse;
import io.grpc.stub.StreamObserver;

public class PatientService extends PatientSericeGrpc.PatientSericeImplBase {

    @Override
    public void findMyRecord(PatientId request, StreamObserver<ServerResponse> responseObserver) {
        responseObserver.onNext(PseudoDatabase.CONNECTION.findSpecificPatientAndPrepareResponse(request.getId()));
        responseObserver.onCompleted();
    }
}
