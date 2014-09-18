#Itsy Language

**Itsy** is a [python](http://www.python.org) inspired procedural language that I have written to learn and experiment with [Antlr](http://www.antlr.org) and implementing an interpreter. 

## Credits

The initial concepts and implementation are inspired by [Bart Kiers](https://github.com/bkiers) and his [blogs posts on the Tiny Language](http://bkiers.blogspot.nl/2011/03/creating-your-own-programming-language.html). It also borrows the INDENT/DEDENT tokenising technique from [Bart's work in the Python3 Grammer](https://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4)

## Quickstart

You will need java 7 or later from here:

[Download Java here](http://java.com)

Then download the itsy for your os (this is a 430k file)

### On OSX
```
cd /usr/local/lib
sudo curl -O http://hornmicro.com/scott/itsy/itsy-1.0-SNAPSHOT.jar
alias itsy='java -jar /usr/local/lib/itsy-1.0-SNAPSHOT.jar'
```

### On Linux
```
cd /usr/local/lib
wget http://hornmicro.com/scott/itsy/itsy-1.0-SNAPSHOT.jar
alias itsy='java -jar /usr/local/lib/itsy-1.0-SNAPSHOT.jar'
```

Then just run your itsy file:

```
itsy filename.it
```


## Building with Maven

First, clone this repository:

```
git clone https://github.com/sjhorn/itsy-lang.git
cd itsy-lang
```

Then generate the lexer, parser and visitor classes using the antlr4 Maven plugin:

```
mvn package
```

To run the tests
```
java -jar target/itsy-1.0-SNAPSHOT.jar
```
 
 This will print the following
 
```
All Assertions have passed.
``` 

Or you can run the fun file

```
java -jar target/itsy-1.0-SNAPSHOT.jar src/main/resources/itsy/fun.it
```

and this will print

```
Loop 1 Mr. Scott
Loop 2 Mr. Scott
Loop 3 Mr. Scott
.
.
.
Loop 98 Mr. Scott
Loop 99 Mr. Scott
Loop 100 Mr. Scott
```





## No Maven?

If you're unfamiliar with Maven, and are reluctant to install it, here's how
to perform all the steps from the (*nix) command line (assuming you're in the
root folder of the project `itsy-lang`):

Download ANTLR 4:

```
wget http://www.antlr.org/download/antlr-4.1-complete.jar
```

Generate the lexer, parser and visitor classes and move them to the other 
`.java` project sources:

```
java -cp antlr-4.1-complete.jar \
  org.antlr.v4.Tool src/main/antlr4/itsy/antlr4/Itsy.g4 \
  -package  \
  -visitor
  
mv src/main/antlr4/itsy/antlr4/*.java src/main/java/itsy/antlr4
```

Compile all `.java` source files:

```
javac -cp antlr-4.1-complete.jar src/main/java/itsy/antlr4/*.java
javac -cp antlr-4.1-complete.jar:src/main/java src/main/java/itsy/lang/*.java
```

Run the `Main` class:

```
java -cp src/main/java:antlr-4.1-complete.jar itsy.lang.Itsy
```


## Language in action

The [test.it](https://github.com/sjhorn/itsy-lang/blob/master/src/main/itsy/test.it) file shows some of the language features.

Below is a sample of Itsy in action.

```
def sayHello(name)
	println "Hello "+name
	
def factorial(n)
	if n < 2
		return 1
	return n * factorial(n-1)
	
sayHello("Scott")

println "10 Factorial is "+factorial(10)

assert "test" in ["one","two","test","three"]	

a = 1
b = 10
if a < b
	println "a "+a+" is less than "+b
else
	println  "a "+a+" is greater than or equal to "+b	
for i = 1 to 10
	println i+". Say it again"
	
for i in [1,3,5,7,9]
	println "How odd "+i
	
facts = ["name":"Scott", "likes to": "cycle", "lives in": "Brisbane"]
for key in facts 
	println key + " -> " + facts[key] 
	
b = input("Do you like programming (y/n)?")
if b == "y"
	println "Cool, me too"
else 
	println "That's because you haven't tried Itsy"

println file("pom.xml")
```