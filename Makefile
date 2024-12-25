LIBS = 
SRC = */*.java *.java

# Compilar todas as classes
main:
	javac $(LIBS) $(SRC)

# Limpar arquivos compilados
clean:
	rm -f */*.class
	rm -f *.class
	rm -f Resultados/*.txt
