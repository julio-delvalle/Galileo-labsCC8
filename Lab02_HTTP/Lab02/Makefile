JFLAGS = -g
JC = javac
JVM= java
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		Request.java \
		Response.java \
		FormatterWebServer.java \
		ThreadServer.java \
		Server.java 

MAIN = Server

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
		$(JVM) $(MAIN) $(ARGS)

clean:
		$(RM) *.class
		$(RM) logs/*