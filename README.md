#Itsy Language

**Itsy** is a [python](http://www.python.org) inspired procedural language that I have written to learn and experiment with [Antlr](http://www.antlr.org) and implementing an interpreter. 

## Credits

The initial concepts and implementation are inspired by [Bart Kiers](https://github.com/bkiers) and his [blogs posts on the Tiny Language](http://bkiers.blogspot.nl/2011/03/creating-your-own-programming-language.html). It also borrows the INDENT/DEDENT tokenising technique from [Bart's work in the Python3 Grammer](https://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4)


## Running

First, clone this repository:

```
git clone https://github.com/sjhorn/itsy-lang.git
cd itsy-lang
```

Then generate the lexer, parser and visitor classes using the antlr4 Maven plugin:

```
mvn antlr4:antlr4
```

Compile all classes:

```
mvn install
```

To run the tests

```
 mvn -q exec:java
```
 
 This will print the following
 
```
All Assertions have passed.
``` 

Our you can run the fun file

```
mvn -q exec:java -Dexec:args="src/main/itsy/fun.it"
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

```bash
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


