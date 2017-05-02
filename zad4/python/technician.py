import grpc
import sys

import MedicalData_pb2
import MedicalData_pb2_grpc

channel = grpc.insecure_channel('localhost:50051')
stub = MedicalData_pb2_grpc.TechServiceStub(channel)


def handle_add():
    id_patient = int(raw_input("Provide ID of patient: "))
    id_doctor = int(raw_input("Provide ID of doctor: "))
    date = raw_input("Provide date of test: ")
    not_finished = True
    params = []

    get_param(params)
    while not_finished:
        decision = raw_input("Add next(\"add\")/finish(\"fin\")")
        if decision == "fin":
            not_finished = False
        elif decision == "add":
            get_param(params)
    req = MedicalData_pb2.TestResult(patientId=id_patient, doctorId=id_doctor, date=date, parameters=params)
    res = stub.AddResultToSystem(req)
    if res.code == 2:
        print(res.msg)
    else:
        print("Added")


def get_param(params):
    name = raw_input("Provide name of parameter: ")
    value = float(raw_input("Provide value of parameter: "))
    unit = raw_input("Provide unit: ")
    params.append(MedicalData_pb2.Parameter(name=name, value=value, unit=unit))


while True:
    operation = raw_input("Provide operation (add/exit):\n")
    if operation == "exit":
        sys.exit(0)
    elif operation == "add":
        handle_add()
    else:
        print("Invalid operation. Try again")
