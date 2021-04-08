# Extensible mutation engines for PIT

Mutation testing is a technique that evaluates the quality of our tests. 
It inserts artificial bugs to create *mutants* of the original code and then 
checks if our tests fail with those mutants.
The models of artificial bugs are known as *mutation operators*.
They can be, for example, the replacement of an addition by a subtraction
or even replacing the entire code of a method.

[PIT or PITest](http://pitest.org/) is a tool that implements mutation testing for JVM projects.
In PIT, a mutation engine handles the mutant creation and the mutation operators. 
The tool comes with a default mutation engine named Gregor. 
I maintain mutation engine named [Descartes](https://github.com/STAMP-project/pitest-descartes/) 
which implements extreme mutation.

Neither Gregor nor Descartes are extensible with custom mutation operators 
or can be used at the same time for the same project.

Being able to add custom mutation operators allows us to extend the capabilities of the default mutation engine. 
Like this we could, for example, tailor mutation operators to our own custom types 
(See https://github.com/hcoles/pitest/issues/859). 
Then, if we have custom mutations, it would be desirable to use them together with the engines above.

This repository fills this gap with two new mutation engines for PIT:
 - **ivo**: which lets you combine more than one mutation engine and use them at the same time 
 - **amazo**: which lets you add custom mutation operators as plugins, without having to fork or recompile PIT or even
  creating your own mutation engine.

With **ivo** it is possible to use several mutation engines with their default configuration 
or even pick a selection of mutation operators from different engines. 
Check the [`pit-descartes` profile](./example-project/pom.xml) for an example.

**amazo** includes abstractions to ease the creation and testing of new mutation operators. 
These abstractions are classes that can be extended to create custom mutation operators
following the most common mutation operator patterns.
In this way you can focus on your own implementation without having to deal with PIT's internal machinery. 
At the moment there are abstractions for the following patterns:

- replace a single bytecode instruction by another, extending 
    [InstructionReplacementOperator](./amazo/src/main/java/io/pitex/engines/amazo/operators/InstructionReplacementOperator.java)
- remove a single bytecode instruction, extending 
    [InstructionRemovalOperator](./amazo/src/main/java/io/pitex/engines/amazo/operators/InstructionRemovalOperator.java)
- completely rewrite the code of a method, extending 
    [MethodRewritingOperator](./amazo/src/main/java/io/pitex/engines/amazo/operators/MethodRewritingOperator.java)
- modify the result of a method, extending 
    [MethodResultOperator](./amazo/src/main/java/io/pitex/engines/amazo/operators/MethodResultOperator.java)

If these mutation operator patterns do not fit your needs you can still extend [MutationOperator](./amazo/src/main/java/io/pitex/engines/amazo/operators/MutationOperator.java).

More abstractions for mutation operators will be included in the near future to support, 
for example, the replacement  of a sequence of bytecode instructions.
The documentation shall be expanded as well.

Check [example-custom-operators](./example-custom-operators) to see how to create a jar with custom mutation
operators ready to use with **amazo**.

Check the [example-project](./example-project/pom.xml) to see how these engines can be used with PIT.
Each use case is presented as a Maven profile, in the [`pom.xml`](./example-project/pom.xml).
For example, `custom-operators` shows how to use **amazo** with the operators implemented in 
[example-custom-operators](./example-custom-operators).

Build this entire repository with `mvn clean install`. 
Then, check the examples in `example-project` by doing `mvn org.pitest:pitest-maven:mutationCoverage -P <profile>`
replacing `<profile>` by one of the existing profile names, for instance: `pit-descartes` or `custom-operators`.

