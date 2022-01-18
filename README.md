# JdbcOracle - SIARD 2.2 Oracle JDBC Wrapper

This package contains the JDBC Wrapper for Oracle DBMS for SIARD 2.2.


## Getting started (for devs)
For building the binaries, Java JDK (1.8 or higher), Ant, and Git must
have been installed. Adjust build.properties to your local configuration. In it using a text editor the local values must be
entered as directed by the comments.

A running instance of Oracle DB is needed to run the tests - unfortunatly, oracle does not provide any ready tu use docker images and you have to build it yourself
But we will help you with this! Just run:

```shell
./docker/build-oracle-image.sh
```

Note: this will clone the necessary repository to build the docker image into the project folder. It will also download the necessary binaries to build the oracle 18xe image - and building the image will take a some time. Go and grab a coffee now!

```shell
docker-compose up -d
```

Run all tests

```shell
ant test
```

Build the project

```shell
ant build
```

This task increments the version number in the project [MANIFEST.MF](./src/META-INF/MANIFEST.MF)


## Documentation
[./doc/manual/user/index.html](./doc/manual/user/index.html) contains the manual for using the binaries.
[./doc/manual/developer/index.html](./doc/manual/user/index.html) is the manual for developers wishing
build the binaries or work on the code.

More information about the build process can be found in
[./doc/manual/developer/build.html](./doc/manual/developer/build.html)