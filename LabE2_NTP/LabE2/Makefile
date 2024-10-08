JFLAGS = -g
# JLIB = -classpath ".;sqlite-jdbc-3.7.2.jar"
JC = javac
JVM= java
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		DNSServer.java \
		Client.java \
		FormatterWebServer.java \
		ThreadServer.java \
		DNSResponse.java \
		DNSRequest.java \

MAIN = DNSServer

default: classes

classes: $(CLASSES:.java=.class)

run:  $(MAIN).class
		$(JVM) $(JLIB) $(MAIN) $(ARGS)

clean:
		$(RM) *.class
		$(RM) logs/*
		$(RM) SMTP_SERVER.db


exampleDB: $(SQLite).class
		$(JVM) $(JLIB) $(SQLite)

