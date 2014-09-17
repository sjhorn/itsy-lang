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

The [test.it](https://github.com/sjhorn/itsy-lang/blob/master/src/main/itsy/test.it) file shows some of the language features and below is the complete file. 

```
/*
    A script for testing Itsy Language syntax.
*/

// boolean expressions
assert true || false
assert !false
assert true && true
assert !true || !false
assert true && (true || false)

// relational expressions
assert 1 < 2
assert 666 >= 666
assert -5 > -6
assert 0 >= -1
assert 'a' < 's'
assert 'sw' <= 'sw'

// add
assert 1 + 999 == 1000
assert [1] + 1 == [1,1]
assert 2 - -2 == 4
assert -1 + 1 == 0
assert 1 - 50 == -49
assert [1,2,3,4,5] - 4 == [1,2,3,5]

// multiply
assert 3 * 50 == 150
assert 4 / 2 == 2
assert 1 / 4 == 0.25
assert 999999 % 3 == 0
assert -5 * -5 == 25
assert [1,2,3] * 2 == [1,2,3,1,2,3]
assert 'ab'*3 == "ababab"

// power
assert 2^10 == 1024
assert 3^3 == 27

// for- and while statements
a = 0
for i=1 to 10
  a = a + i
assert a == (1+2+3+4+5+6+7+8+9+10)

b = -10
c = 0
while b < 0 
  c = c + b
  b = b + 1
assert c == -(1+2+3+4+5+6+7+8+9+10)

// if
a = 123
if a > 200
  assert false

if a < 100
  assert false
else if a > 124
  assert false
else if a < 124
  assert true
else
  assert false

if false
  assert false
else
  assert true

// functions
def twice(n)
  temp = n + n 
  return temp

def squared(n) 
  return n*n 

def squaredAndTwice(n) 
  return twice(squared(n)) 
def list()
  return [7,8,9]
       
assert squared(666) == 666^2
assert twice(squared(5)) == 50
assert squaredAndTwice(10) == 200
assert squared(squared(squared(2))) == 2^2^2^2
assert list() == [7,8,9]
assert size(list()) == 3
assert list()[1] == 8

// naive bubble sort
def sort(list)
  while !sorted(list)
  	//nothing
def sorted(list)
  n = size(list)
  for i=0 to n-2
    if list[i] > list[i+1]
      temp = list[i+1]
      list[i+1] = list[i]
      list[i] = temp
      return false
  return true

numbers = [3,5,1,4,2]
sort(numbers)
assert numbers == [1,2,3,4,5]

// resursive calls
def fib(n)
  if n < 2
    return n
  else
    return fib(n-2) + fib(n-1)
sequence = []
for i = 0 to 10
  sequence = sequence + fib(i)
assert sequence == [0,1,1,2,3,5,8,13,21,34,55]

// lists and lookups, `in` operator
n = [[1,0,0],[0,1,0],[0,0,1]]
p = [-1, 'abc', true]
       
assert 'abc' in p
assert [0,1,0] in n
assert n[0][2] == 0
assert n[1][1] == n[2][2]
assert p[2]
assert p[1][2] == 'c'

println "All Assertions have passed."
```