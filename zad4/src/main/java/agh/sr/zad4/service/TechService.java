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
                    ServerResponse
                    .newBuilder()
                    .setCode(ServerResponse.ServerResponseCode.NOT_FOUND)
                    .setMsg("Patient isn't in database.")
                    .build()
            );
        }
        else{
            PatientRecord pr = PseudoDatabase.CONNECTION.getPatientRecordBase().get(request.getPatientId());
            pr = pr.toBuilder()
                    .addResults(request)
                    .build();
            PseudoDatabase.CONNECTION.getPatientRecordBase().put(request.getPatientId(),pr);
            pr.getResultsList()
                    .forEach(e -> {
                        System.out.println(e.getPatientId()+" "+e.getDate()+" "+e.getDoctorId());
                        e.getParametersList()
                                .forEach(p -> System.out.println("\t"+p.getName()+" "+p.getValue()+" "+p.getUnit()));
                    });
            responseObserver.onNext(
                    ServerResponse
                    .newBuilder()
                    .setCode(ServerResponse.ServerResponseCode.CREATED)
                    .build()
            );
        }
        responseObserver.onCompleted();
    }

}
