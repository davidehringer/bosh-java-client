# Java Client for BOSH

[bosh.io](http://bosh.io)

This client is currently in an beta state and subject to significant changes.

## Usage

The BOSH Director API is primarily an asynchronous, task-based API and many operations can be long running. The bosh-java-client
uses [RxJava](https://github.com/ReactiveX/RxJava) [Observables](http://reactivex.io/intro.html) as the return type for all API
calls to allow the client applications to choose how they wish to deal with the asynchronous nature of the BOSH API.

The [DirectorClient](src/main/java/io/bosh/client/DirectorClient.java) is the entry point into the API. Each domin area has its
own interface that you can access through the `DirectorClient`.  To create instances of the `DirectorClient`, use 
the [SpringDirectorClientBuilder](src/main/java/io/bosh/client/SpringDirectorClientBuilder.java)

```
DirectorClient client = new SpringDirectorClientBuilder()
                .withHost("192.168.50.4")
                .withCredentials("admin", "admin")
                .build();
```

You can consume the [Observables](http://reactivex.io/RxJava/javadoc/rx/Observable.html) in a variety of ways such as
```
Deployments deployments = client.deployments();
deployments.list().subscribe(deploymentList -> {
            
			// ...
        });         

// or
Deployments deployments = client.deployments();
deployments.list().subscribe(deploymentList -> {
    // ...
    }, error -> {
        // handle error
    });
```
or
```
FetchLogsRequest request = new FetchLogsRequest()
        .withDeploymentName("my-deployment")
        .withJobName("my-job")
        .withJobIndex(2)
        .withLogType(LogType.JOB);
InputStream logs = client.jobs().fetchLogs(request).toBlocking().first();
// do something with the logs
```

## Compatibility

The BOSH team does not currently consider the Director API to be public and it may be subject to breaking changes
between releases. Please test against the specific version of BOSH that you are running.

## Logging

The project uses commons-logging. To enable logging of all the calls (the URL being called, not the payload)
to the BOSH Director, set the logging level of the `BOSH_Director_API` to `INFO`.

## Continuous Integration

The CI server for the project is hosted at https://gaptap.atlassian.net/builds/browse/BJC-BOS.