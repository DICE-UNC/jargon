# Docker iRODS grid test/build tools

The docker-test-framework subdirectory includes entries for various iRODS versions. Upon selection of a version, the docker-compose up command 
can be issued from that subdirectory

e.g.

```

cd docker-test-framework
cd 4-2
docker-compose build
docker-compose up

```

This should start an iRODS server and resource server on a Docker private network. This will also build and run a maven test instance. This is 
a JDK 11 and Maven enabled container that can be accessed via a shell. The container mounts the jargon source code and is pre-configured with maven settings to match the iRODS containers.

Once iRODS is running, it can be addressed via port 1247 for most operations, though parallel transfers via ephemeral ports is difficult from the local host. For this reason, a Dockerfile is provided at the root that can be build and run. This contains OpenJDK11 and maven and mounts the jargon source code as a Docker volume. This allows interactive coding and testing.

For illustration here is a view of a running test framework

```
Last login: Tue Apr 14 12:23:14 on ttys011
docker ps
(base) ~/Documents/workspace-niehs-rel/jargon/docker-test-framework/4-2 @ ALMBP-02010755(conwaymc): docker ps
CONTAINER ID        IMAGE                                  COMMAND                  CREATED             STATUS              PORTS                              NAMES
2d94c58fd077        4-2_irods-catalog-consumer-resource1   "./start_consumer.sh"    22 minutes ago      Up 22 minutes       1247-1248/tcp                      irods-catalog-consumer-resource1
072086ac44e1        4-2_irods-catalog-provider             "./start_provider.sh"    22 minutes ago      Up 22 minutes       0.0.0.0:1247->1247/tcp, 1248/tcp   irods-catalog-provider
c7e102bd061d        4-2_maven                              "/usr/local/bin/mvn-…"   22 minutes ago      Up 22 minutes                                          maven
(base) ~/Documents/workspace-niehs-rel/jargon/docker-test-framework/4-2 @ ALMBP-02010755(conwaymc): 



```

To build and test, simply cd to /usr/src/jargon and run mvn commands after logging into the test container. This snippet illustrates logging in and positioning to the top of the Jargon project, ready to issue maven commands

```
(base) ~/Documents/workspace-niehs-rel/jargon/docker-test-framework/4-2 @ ALMBP-02010755(conwaymc): docker exec -it maven sh
# cd /usr/src/jargon
# ls
CHANGELOG.md  README.md     docker-build-test.sh  docker-test-framework   jargon-core	     jargon-mdquery  jargon-ruleservice  jargon-user-tagging  pom.xml	    target
LICENSE.txt   data-profile  docker-build.sh	  eclipse-formatting.xml  jargon-data-utils  jargon-pool     jargon-ticket	 jargon-zipservice    settings.xml  user-guide
# 

```

Here's a helpful command for running specific tests.

```
mvn test -Dtest='<class>#<method>,<class>#<method>, ...' -DfailIfNoTests=false -pl <module> -am
```

Below is an example demonstrating how to run two tests in the jargon-core module.

```
mvn test \
    -Dtest='DataObjectAOImplTest#testReplicaTruncateNoTargetReplica,DataObjectAOImplTest#testReplicaTruncateInvalidInputs' \
    -DfailIfNoTests=false \
    -pl jargon-core \
    -am
```


Note the settings.xml file is mounted that has the correct coordinates for the iRODS grid pre-configured with test accounts, resources, groups, etc as expected by the Jargon unit test framework.

