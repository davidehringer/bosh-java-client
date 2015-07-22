# Java Client for BOSH

[bosh.io](http://bosh.io)

This client is currently in an beta state and subject to significant changes. Only read-only
are supported.

## Usage

``` 
DirectorOperations director = new DirectorClient("192.168.50.4", "admin", "admin");

System.out.println(director.getDeployments());
System.out.println(director.getDeployment("my-deployment").getRawManifest());

InputStream is = director.fetchLogs("my-deployment-backup", "my-job", 0, LogType.AGENT);
Files.copy(is, Paths.get("./my-job-logs.tgz"));
```

See `DirectorOperations` for the full API.

## Compatibility

The BOSH team does not currently consider the Director API to be public and it may be subject to breaking changes
between releases. Please test against the specific version of BOSH that you are running.