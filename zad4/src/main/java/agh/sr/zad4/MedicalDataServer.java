package agh.sr.zad4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import agh.sr.zad4.proto.Parameter;
import agh.sr.zad4.proto.PatientRecord;
import agh.sr.zad4.proto.TestResult;
import agh.sr.zad4.service.DoctorService;
import agh.sr.zad4.service.PatientService;
import agh.sr.zad4.service.PseudoDatabase;
import agh.sr.zad4.service.TechService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class MedicalDataServer {
    private static final Logger logger = Logger.getLogger(MedicalDataServer.class.getName());

    private static List<String> PARAM_TYPES = Arrays.asList("haemoglobin", "fat", "iron", "hpl", "lmv", "kpc");
    private static List<Double> PARAM_MINS = Arrays.asList(10d,0.4d,100d,4.5d,166.3d,144.5d);
    private static List<Double> RANGE = Arrays.asList(10d,0.4d,200d,3d,33.7d,200d);
    private static List<String> PARAM_UNITS = Arrays.asList("mg", "dl", "mg/l", "ls/h", "%", "%");

    private int port = 50051;
    private Server server;

    private void start() throws IOException
    {
        server = ServerBuilder.forPort(port)
                .addService(new TechService())
                .addService(new PatientService())
                .addService(new DoctorService())
                .build()
                .start();

        logger.info("MedicalDataServer started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                MedicalDataServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final MedicalDataServer server = new MedicalDataServer();
        generatePatients();
        server.start();
        server.blockUntilShutdown();
    }


    public static void generatePatients() {
        try {
            Files.readAllLines(Paths.get("./CSV_Database_of_Last_Names.csv")).forEach(MedicalDataServer::generatePatient);
            prepareResults();
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private static void generatePatient(String name){
        PatientRecord pr =  PatientRecord
                .newBuilder()
                .setData("Jacek "+name)
                .build();
        PseudoDatabase.CONNECTION.addPatientRecord(pr);
    }

    private static void prepareResults() {
        Random random = new Random();
        PseudoDatabase.CONNECTION.getPatientRecordBase().values().forEach(e -> {
            int res = random.nextInt(60)+10;
            List<TestResult> prList = new ArrayList<>(res);
            for (int i = 0; i < res; i++) {
                prList.add(TestResult
                        .newBuilder()
                        .setDate("2017/04/11")
                        .setDoctorId(Math.abs(random.nextInt()))
                        .setPatientId(e.getId())
                        .addAllParameters(prepareParams())
                        .build());
            }
            PseudoDatabase.CONNECTION.getPatientRecordBase().put(e.getId(),e.toBuilder().addAllResults(prList).build());

        });

    }

    private static List<Parameter> prepareParams() {
        Random random = new Random();
        int res = random.nextInt(PARAM_TYPES.size()-1)+1;
        List<Parameter> params = new ArrayList<>(res);
        for(int i = 0 ; i < res; i++){
            params.add(
                    Parameter
                            .newBuilder()
                            .setName(PARAM_TYPES.get(i))
                            .setValue((Math.random()* RANGE.get(i)+ PARAM_MINS.get(i))*(Math.random()*0.5 + 0.75))
                            .setUnit(PARAM_UNITS.get(i))
                            .build()
            );
        }
        return params;
    }
}
