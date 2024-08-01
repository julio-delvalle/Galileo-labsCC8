JFLAGS = -g
JLIB = -classpath ".;sqlite-jdbc-3.7.2.jar"
JC = javac
JVM= java
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		SMTPServer.java \
		SMTPClient.java \
		FormatterWebServer.java \
		IMAPServer.java \
		SQLiteJDBC.java 

MAIN = IMAPServer
SMTPSERVER = SMTPServer
SMTPCLIENT = SMTPClient
SQLite = SQLiteJDBC

default: classes

classes: $(CLASSES:.java=.class)

run:  $(MAIN).class
		$(JVM) $(JLIB) $(MAIN) $(ARGS)

clean:
		$(RM) *.class
		$(RM) logs/*
		$(RM) SMTP_SERVER.db

smtpserver: $(SMTPSERVER).class
		$(JVM) $(JLIB) $(SMTPSERVER)

smtpclient: $(SMTPCLIENT).class
		$(JVM) $(JLIB) $(SMTPCLIENT)

exampleDB: $(SQLite).class
		$(JVM) $(JLIB) $(SQLite)

