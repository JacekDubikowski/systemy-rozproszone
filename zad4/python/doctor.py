import grpc

import MedicalData_pb2
import MedicalData_pb2_grpc


def get_param(parameters):
    name = raw_input("Provide name of parameter: ")
    minimum = get_float("Provide min of parameter: ")
    maximum = get_float("Provide max of parameter: ")
    parameters.append(MedicalData_pb2.Parameter(name=name, min=minimum, max=maximum))


def get_integer(string):
    integer = raw_input(string)
    while not integer.isdigit():
        integer = raw_input("Try again. " + string)
    return int(integer)


def get_float(string):
    def try_get_float(a):
        try:
            float(a)
            return True
        except ValueError:
            return False
    floating = raw_input(string)
    while not try_get_float(floating):
        floating = raw_input("Try again. " + string)
    return float(floating)


channel = grpc.insecure_channel('localhost:50051')
stub = MedicalData_pb2_grpc.DoctorServiceStub(channel)

id_patient = get_integer("Provide ID of patient (or 0 if you want to check all of them): ")
not_finished = True
params = []

while not_finished:
    decision = raw_input("Add parameter to check(\"add\")/finish adding and send query(\"fin\"): ")
    if decision == "fin":
        not_finished = False
    elif decision == "add":
        get_param(params)

query = MedicalData_pb2.QueryParams(patientId=id_patient, params=params)

try:
    for res in stub.findRecords(query):
        if res.code == 2:
            print res.msg
        else:
            record = res.record
            print(str(record.id) + " " + record.data)
            for r in record.results:
                print("\t"+str(r.patientId)+" "+r.date+" "+str(r.doctorId))
                for t in r.parameters:
                    print("\t\t"+t.name+" "+str(t.value)+" "+t.unit)
    print("Finished")
except grpc._channel._Rendezvous:
    print("No connection lost.")