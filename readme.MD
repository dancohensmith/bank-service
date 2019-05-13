# Bank Service

## Purpose

This service allows users to transfer values between accounts using a HTTP REST service.

Assumptions:

* When the service starts accounts are already created with starting balances.
* Accounts can't hold negative balances.

## Design

This service uses [Javalin](https://javalin.io/) as the REST framework. It supports asynchronous handling of REST Requests.

I raised a PR against [Javalin](https://javalin.io/) to improve the exception handling so that the underlying cause is
unrwapped from the CompletionException thrown if there is a failure in execution of a CompletableFuture/Future. this has
resulted in including on dependency using [Jitpack](https://jitpack.io/).

The server allows concurrent processing of requests by handing off the request to a SingleThreadedExecutor and returning 
a CompletableFuture that completes when the request is complete or fails.

Other options where considered such as wrapping the AccountBalance with the ability to lock the two balances. To achieve 
we would need to order the locking using the accountId to order. this would ensure we would always lock in the same order. 
The decision was this was much more complicated and not neccessary.

## Running service

use Maven to build the service.
```
mvn clean install
```
run 
```
java -jar target/bank-service-1.0-SNAPSHOT.jar
```

## Postman collection

There is a [postman collection](src/test/resources/bank-service.postman_collection.json) to test the service with.
