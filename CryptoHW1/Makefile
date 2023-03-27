#Makefile - CryptoHW1
#Targets and what they do:
#CryptoHW creates the executable
#*.o targets compile .cpp and .h files into an o file
#clean removes all files and executables

CXXSOURCE = main.o RC4.o blockCypher.o
HEADERS = RC4.hpp blockCypher.hpp
CXXFLAGS = --std=c++14
COMPILER = c++
COMPILEOPTS = -c
LINKINGOPTS = -o

run: CryptoHW
	./CryptoHW

CryptoHW: $(CXXSOURCE)
	$(COMPILER) $(CXXFLAGS) $(LINKINGOPTS) CryptoHW $(CXXSOURCE)

.PHONY: clean

main.o: main.cpp $(HEADERS)
	$(COMPILER) $(CXXFLAGS) $(COMPILEOPTS) main.cpp

RC4.o: RC4.cpp $(HEADERS)
	$(COMPILER) $(CXXFLAGS) $(COMPILEOPTS) RC4.cpp

blockCypher.o: blockCypher.cpp $(HEADERS)
	$(COMPILER) $(CXXFLAGS) $(COMPILEOPTS) blockCypher.cpp

clean:
	rm -rf *.o CryptoHW
