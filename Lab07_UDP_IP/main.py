from socket import *

serverName = '127.0.0.1'
serverPort = 3000

clientSocket = socket(AF_INET, SOCK_RAW, IPPROTO_RAW)

print(f"AF_INET: {AF_INET}")
print(f"SOCK_DGRAM: {SOCK_DGRAM}")
print(str(SOCK_DGRAM))

message = input('Input lowercase sentence:')