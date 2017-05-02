import grpc

import MedicalData_pb2
import MedicalData_pb2_grpc

try:
    channel = grpc.insecure_channel('localhost:50051')
    stub = MedicalData_pb2_grpc.PatientSericeStub(channel)

    my_id = int(raw_input("Provide yours ID:\n"))
    res = stub.findMyRecord(MedicalData_pb2.PatientId(id=my_id))

    if res.code == 2:
        print(res.msg)
    elif res.code == 0:
        print(res.msg)
        record = res.record
        print(record.data)
        for r in record.results:
            print("\t"+str(r.patientId)+" "+r.date+" "+str(r.doctorId))
            for t in r.parameters:
                print("\t\t"+t.name+" "+str(t.value)+" "+t.unit)
    else:
        print("Server provided not proper answer")
except ValueError:
    print("That's not an id. Re-run with proper id.")
except grpc._channel._Rendezvous:
    print("No connection.")
