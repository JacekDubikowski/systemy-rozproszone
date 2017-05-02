package agh.sr.zad4.service;

import agh.sr.zad4.proto.PatientRecord;
import agh.sr.zad4.proto.ServerResponse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public enum PseudoDatabase {
    CONNECTION;

    private final Map<Integer,PatientRecord> patientRecordBase = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(1);

    public synchronized ServerResponse addPatientRecord(PatientRecord pr){
        if(pr.getId()==0){
            pr = pr.toBuilder().setId(counter.getAndIncrement()).build();
            patientRecordBase.put(pr.getId(),pr);
            return ServerResponse
                    .newBuilder()
                    .setCode(ServerResponse.ServerResponseCode.CREATED)
                    .setRecord(pr)
                    .build();
        }
        else return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.ERROR)
                .setMsg("Patient cannot have id before being added")
                .build();
    }

    public Map<Integer, PatientRecord> getPatientRecordBase() {
        return patientRecordBase;
    }

    public Optional<PatientRecord> findParticularPatientRecord(int id){
        return Optional.ofNullable(patientRecordBase.get(id));
    }

    public ServerResponse findSpecificPatientAndPrepareResponse(int id){
        if(!patientRecordBase.containsKey(id)){
            return prepareNotFoundMsg();
        }
        else{
            PatientRecord pr = patientRecordBase.get(id);
            return ServerResponse
                    .newBuilder()
                    .setCode(ServerResponse.ServerResponseCode.OK)
                    .setMsg("Found in database")
                    .setRecord(pr)
                    .build();
        }
    }

    public static ServerResponse prepareNotFoundMsg(){
        return ServerResponse
                .newBuilder()
                .setCode(ServerResponse.ServerResponseCode.NOT_FOUND)
                .setMsg("Patient isn't in database.")
                .build();
    }
}
