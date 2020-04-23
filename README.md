## monad-ui

Exploring different UIs for a simple Monad to evaluate the amount of code generated.

  - [Output](src/main/scala/monadui/Output.scala) data type (writer + option effects)
  - [DSL implementations](src/main/scala/monadui)
  - Client code in [OutputTest](src/test/scala/monadui/OutputTest.scala)
  
## Results

```

================================================================================
testFlatMapDsl
1 classes, 8743 bytes
monadui/OutputTest$testFlatMapDsl$.class
Output(Some(55),Map(Source -> Vector(OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest)))

================================================================================
testFlatMapDslWithoutIndyLambda
11 classes, 27717 bytes
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$14$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$15$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$17$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$16$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$13$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$12$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$19$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$18$1.class
monadui/OutputTest$testFlatMapDslWithoutIndyLambda$$anonfun$monadui$OutputTest$testFlatMapDslWithoutIndyLambda$$$nestedInanonfun$result$11$1.class
Output(Some(55),Map(Source -> Vector(OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest)))

================================================================================
testFlatMapMacroDsl
1 classes, 2909 bytes
monadui/OutputTest$testFlatMapMacroDsl$.class
Output(None,Map(Source -> Vector(OutputTest)))

================================================================================
testImplicitDsl
1 classes, 4268 bytes
monadui/OutputTest$testImplicitDsl$.class
Output(Some(55),Map(Source -> Vector(OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest)))

================================================================================
testAsyncDsl
2 classes, 7032 bytes
monadui/OutputTest$testAsyncDsl$stateMachine$async$1.class
monadui/OutputTest$testAsyncDsl$.class
Output(Some(55),Map(Source -> Vector(OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest, OutputTest)))
```

