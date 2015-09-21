# Java Client for BOSH

[bosh.io](http://bosh.io)

This client is currently in an beta state and subject to significant changes. Only read-only
are supported.

## v2 Usage

```
DirectorClient client = new SpringDirectorClientBuilder()
                .withHost("192.168.50.4").withCredentials("admin", "admin").build();

Deployments deployments = client.deployments();
deployments.list().subscribe(response -> {
            List<Deployment> deps = response.getDeployments();
			// ...
        });         
```

## Compatibility

The BOSH team does not currently consider the Director API to be public and it may be subject to breaking changes
between releases. Please test against the specific version of BOSH that you are running.

## Continuous Integration

https://gaptap.atlassian.net/builds/browse/BJC-BOS