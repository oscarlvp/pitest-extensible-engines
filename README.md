# Extensible mutation engines for PIT

Mutation testing is a technique that evaluates the quality of our tests. 
It inserts artificial bugs to create *mutants* of the original code and then 
checks if our tests fail with those mutants.
The models of artificial bugs are known  as *mutation operators*.

[PIT](http://pitest.org/), or PITest, is a tool that implements mutation testing for JVM projects.
In PIT, a mutation engine handles the mutant creation and the mutation operators. 
The tool comes with a default mutation engine named Gregor. 
I maintain mutation engine named [Descartes](https://github.com/STAMP-project/pitest-descartes/) 
which implements extreme mutation.

Neither Gregor nor Descartes are extensible with custom mutation operators or can be used at the same time.

Being able to add custom mutation operators allows us to extend the capabilities of the default mutation engine. 
Like this we could, for example, tailor mutation operators to our own custom types 
(See https://github.com/hcoles/pitest/issues/859). 
Then, if we have custom mutations, it would be desirable to use them together with the engines above.

This repository fills this gap with two new mutation engines for PIT:
 - **ivo**: which lets you combine more than one mutation engine and use them at the same time 
 - **amazo**: which lets you add custom mutation operators as plugins, without having to fork or recompile PIT or even
  creating your own mutation engine.

Check the [example-project](./example-project/pom.xml) to see how these engines can be used with PIT.

Check [example-custom-operators](./example-custom-operators) to see how to create a jar with custom mutation operators.

With **ivo** it is possible to use several mutation engines with their default configuration 
or even pick a selection of mutation operators from different engines.

**amazo** includes abstractions to ease the creation and testing of new mutation operators.
In this way you can focus on implementing the mutation operators without having to deal with PIT's internal machinery. 

At the moment you can use 
[InstructionReplacementOperator](./amazo/src/main/java/io/pitex/engines/amazo/InstructionReplacementOperator.java) 
to create operators that replace one bytecode instruction by another and
[MethodRewritingOperator](./amazo/src/main/java/io/pitex/engines/amazo/MethodRewritingOperator.java) 
for mutation operators that replace the body of a method. 

These two abstractions cover most operators included in Gregor and Descartes.

More abstractions will be included in the near future to support, for example, the replacement of a sequence of bytecode
instructions or to affect the result of a method.
