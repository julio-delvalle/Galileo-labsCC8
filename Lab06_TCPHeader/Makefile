JFLAGS = -g
JC = javac
JVM= java
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		Main.java \
		FormatterWebServer.java \

MAIN = Main

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

