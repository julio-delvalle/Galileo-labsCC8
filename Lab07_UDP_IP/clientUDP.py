from socket import *
import struct

serverName = '127.0.0.1'
serverPort = 3000

clientSocket = socket(AF_INET, SOCK_RAW, IPPROTO_RAW)


# Ask user for serverName and serverPort
user_server = input("Servidor de destino (presione Enter para default '127.0.0.1'): ")
serverName = user_server if user_server else serverName

try:
    user_port = int(input("Puerto de destino (presione Enter para default 3000): "))
    serverPort = user_port if 0 < user_port < 65536 else serverPort
except ValueError:
    print("Puerto de destino no válido. Usando puerto predeterminado 3000.")
    pass  # Keep default if input is not a valid integer

print(f"Using server: {serverName}, port: {serverPort}")


myIp = gethostbyname(gethostname())
myPort = 3001

print(f"My IP: {myIp}, My Port: {myPort}")

message = ""




#ESTAS VARIABLES SON FIJAS PARA IPV4
IPHeaderVersion = 4
IPHeaderIHL = 5
# Combine IPHeaderVersion and IPHeaderIHL into a single hex value
IPHeaderVersionIHL = struct.pack('!B', (IPHeaderVersion << 4) | IPHeaderIHL)
print("IPHeaderVersionIHL: ", type(IPHeaderVersionIHL))
print("IPHeaderVersionIHL: ", IPHeaderVersionIHL)
IPHeaderTOS = struct.pack('!B', 0)
IPHeaderTotalLength = struct.pack('!H', IPHeaderIHL + 20)
IPHeaderIdentification = struct.pack('!H', 43690)

#Flags and Fragment Offset se parten 16 bits entre ellos:
IPHeaderFlags = b'000' #3 bits en 0
IPHeaderFragmentOffset = b'0000000000000' #13 bits en 0
FlagsAndFragmentOffset = int(IPHeaderFlags + IPHeaderFragmentOffset, 2)
print("FlagsAndFragmentOffset: ", type(FlagsAndFragmentOffset))

IPHeaderTTL = 255 #255 Time To Live
IPHeaderProtocol = 17 #UDP
print("IPHeaderProtocol: ", IPHeaderProtocol)


while(message != 'EXIT'):
    message = input('Mensaje:')



    # VARIABLES IPV4
    IPHeaderChecksum = 0 # ====== CAMBIAR?? ======
    print("MyIp: ", myIp)
    print("Server: ", serverName)
    splitMyIp = myIp.split('.')
    IPHeaderSourceIP = b''
    for i in splitMyIp: # Separa la ip en bytes, y los une en un solo byte string
        IPHeaderSourceIP = IPHeaderSourceIP+struct.pack('!B', int(i))
    splitServerIp = serverName.split('.')
    IPHeaderDestinationIP = b''
    for i in splitServerIp: # Separa la ip en bytes, y los une en un solo byte string
        IPHeaderDestinationIP = IPHeaderDestinationIP+struct.pack('!B', int(i))

    # B Byte, H Halfword, I Integer, Q Quadruple
    # IPHeader = struct.pack('!BBHII2sHHIQQ',
    # print("IPHeaderVersionIHL: ", IPHeaderVersionIHL)
    # print("IPHeaderTOS: ", IPHeaderTOS)
    # print("IPHeaderTotalLength: ", IPHeaderTotalLength)
    # print("IPHeaderIdentification: ", IPHeaderIdentification)
    # print("FlagsAndFragmentOffset: ", FlagsAndFragmentOffset)
    # print("IPHeaderTTL: ", IPHeaderTTL)
    # print("IPHeaderProtocol: ", IPHeaderProtocol)
    # print("IPHeaderChecksum: ", IPHeaderChecksum)
    # print("IPHeaderSourceIP: ", IPHeaderSourceIP)
    # print("IPHeaderDestinationIP: ", IPHeaderDestinationIP)
    IPHeader = struct.pack('!ss2s2sHBBH4s4s',
                            IPHeaderVersionIHL, #1 byte
                            IPHeaderTOS, #1 byte
                            IPHeaderTotalLength, #2 bytes
                            IPHeaderIdentification, #2 bytes
                            FlagsAndFragmentOffset, #2 bytes
                            IPHeaderTTL, #1 byte
                            IPHeaderProtocol, #1 byte
                            IPHeaderChecksum, #2 bytes  
                            IPHeaderSourceIP, #4 bytes
                            IPHeaderDestinationIP) #4 bytes

    print("IPHeaderType: ", type(IPHeader)) 
    print("IP Header: ", "".join("{:02x}".format(c) for c in IPHeader)) #Imprime en formato HEX


    # UDP Header
    UDPchecksum = 0
    print("My Port: ", myPort)
    print("Server Port: ", serverPort)
    print("Message Length: ", len(message))
    print("UDP Checksum: ", UDPchecksum)
    UDPHeader = struct.pack('!HHHH', myPort, serverPort, len(message), UDPchecksum) #tipo 'bytes'
    # print("UDPHeaderType: ", type(UDPHeader)) 
    print("UDP Header: ", "".join("{:02x}".format(c) for c in UDPHeader)) #Imprime en formato HEX


    packet = IPHeader + UDPHeader

    clientSocket.sendto(packet, (serverName, serverPort))

clientSocket.close()