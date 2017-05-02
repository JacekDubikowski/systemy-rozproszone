import datetime
import grpc
import sys

import MedicalData_pb2
import MedicalData_pb2_grpc

channel = grpc.insecure_channel('localhost:50051')
stub = MedicalData_pb2_grpc.TechServiceStub(channel)


def handle_add():
    id_patient = get_integer("Provide ID of patient: ")
    id_doctor = get_integer("Provide ID of doctor: ")
    date = raw_input("Provide date: ")
    #date = int(raw_input("Provide date as long of test: "))
    #date = str(datetime.datetime.fromtimestamp(date).strftime('%Y/%m/%d'))
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
    try:
        res = stub.AddResultToSystem(req)
        if res.code == 2:
            print(res.msg)
        else:
            print("Added")
    except grpc._channel._Rendezvous:
        print("No connection. Try again later.")


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


def get_param(params):
    name = raw_input("Provide name of parameter: ")
    value = get_float("Provide value of parameter: ")
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
