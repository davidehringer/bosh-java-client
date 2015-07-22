# Java Client for BOSH

bosh.io

Currently experimental.

## Usage

``` 
DirectorOperations director = new DirectorClient("192.168.50.4", "admin", "admin");

System.out.println(director.getDeployments());
System.out.println(director.getDeployment("my-deployment").getRawManifest());

InputStream is = director.fetchLogs("my-deployment-backup", "my-job", 0, LogType.AGENT);
Files.copy(is, Paths.get("./my-job-logs.tgz"));
```



    // https://github.com/cloudfoundry/bosh/blob/84252a136ddc1436bcefbbdc0e16677f1ddcff56/bosh_cli/lib/cli/commands/log_management.rb
    // https://github.com/cloudfoundry/bosh/blob/master/bosh_cli/lib/cli/client/director.rb