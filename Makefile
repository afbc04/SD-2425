LIBS = 
SRC = Client/*.java Server/*.java *.java

# Compilar todas as classes
main:
	javac -cp $(LIBS):. $(SRC)

# Limpar arquivos compilados
clean:
	rm -f */*.class
	rm -f *.class