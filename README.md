# Jenkins APTLY repository publisher plugin
This plugin adds a publisher step to Jenkins, which allows you to publish
debian artifacts to the local aptly repository. At the moment it requires that aptly
repository were present on your Jenkins machine, with already published (in aptly terms)
repository.

## Using with Jenkins Jobs DSL plugin

APTLY repository plugin provides ability to configure publisher via Jenkins Job DSL:

```
job("myjob") {
  ...
  publishers {
    archiveArtifacts "*.deb"
    publishToAptly "aptly_repository"
  }
}
```

## Testing 

Running tests require the following packages to be installed and available in your `$PATH`:
  * `aptly` - for obvious reasons
  * `fpm` - tests use fpm to generate sample package


## License

Copyright © 2016, Control Stack Ltd. Licensed under the Apache 2 License.