#!/usr/local/bin/perl
use Sys::Hostname;
use Socket;
# RawSocket
socket(SOCKET, AF_INET, SOCK_RAW, 255) || die $!;
setsockopt(SOCKET, 0, 1, 1);

# IP/Hostname
my $srcIP = ((gethostbyname($ARGV[0]))[4]);
my $dstIP = ((gethostbyname($ARGV[1]))[4]);

# Ports
my $srcPort = $ARGV[2];
my $dstPort = $ARGV[3];

# Verifique si todos los parámetros están presentes
if (!defined $srcIP or !defined $srcPort or !defined $dstIP or !defined $dstPort) {
print "\n\tUsage: $0 <source ip> <destination ip> <source port> <destination port>\n\n";
}

# Genera los encabezados
my $packet = headers();

# Utilizando RawSocket
my $destination = pack('Sna4x8', AF_INET, $dstPort, $dstIP);

# Enviar > send() y Recibir > recv()
send(SOCKET,$packet,0,$destination);



sub headers {
# Campos IP
my $IP_version = 4;
my $IP_IHL = 5;
my $IP_ToS = '00000000';
my $IP_TotalLength = $IP_IHL + 20;
my $IP_Frag_ID = 0;
my $IP_Frag_Flags = '000';
my $IP_Frag_Offset = '0000000000000';
my $IP_TTL = 0xF;
my $IP_Protocol = 17;
my $IP_checksum = 0;
# Mensaje a enviar en UDP
my $data = 'probar con el server UDP del Lab 1';
# Empacar el mensaje de forma dinámica
my $loadData = pack('a' . length($data), $data); # a: String 
print "loadData: $loadData\n";
# Campos UDP
my $UDP_src_port = $srcPort;
my $UDP_dst_port = $dstPort;
my $UDP_Length = 8 + length($loadData);
my $UDP_checksum = 0;
## http://perldoc.perl.org/functions/pack.html
## Empaquetado de IP - RFC 791
my $IP_header = pack(
'H2 B8 n n B16 h2 c n a4 a4', $IP_version . $IP_IHL, $IP_ToS, $IP_TotalLength,
$IP_Frag_ID, $IP_Frag_Flags . $IP_Frag_Offset, $IP_TTL, $IP_Protocol,
$IP_checksum, $srcIP, $dstIP
);
print "IP_header: $IP_header\n";
## Empaquetado de UDP - RFC 768
my $UDP_header = pack(
'n n n n', $UDP_src_port, $UDP_dst_port, $UDP_Length, $UDP_checksum
);
print "UDP_header: $UDP_header\n";
return $IP_header . $UDP_header . $loadData;
}



# El checksum podrían usar esta función:
sub checksum {
my $msg = shift;
my $length = length($msg);
my $numShorts = $length/2;
my $sum = 0;
foreach (unpack("n$numShorts", $msg)) { $sum += $_; }
$sum += unpack("C", substr($msg, $length - 1, 1)) if $length % 2;
$sum = ($sum >> 16) + ($sum & 0xffff);
return(~(($sum >> 16) + $sum) & 0xffff);
}