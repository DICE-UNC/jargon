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

This should start an iRODS server and resource server on a Docker private network.

Once iRODS is running, it can be addressed via port 1247 for most operations, though parallel transfers via ephemeral ports is difficult from the local host. For this reason, a Dockerfile is provided at the root that can be build and run. This contains OpenJDK11 and maven and mounts the jargon source code as a Docker volume. This allows interactive coding and testing.

To build the local test platform, from the root of this repo. Note you must provide the local absolute path to the Jargon source tree for the mount

```
docker build -t diceunc/jargon-test:latest .
docker run -it -v /LOCALABSOLUTEPATH/jargon:/var/jargon --network="4-2_irodsnet" diceunc/jargon-test:latest 

```

Note as well you might need to adjust the --network name to the correct docker local network name, this can be resolved by calling docker inspect on the irods-catalog-provider container, this allows the Docker build/test container to attach to the docker-compose grid.

When the Docker build/test container runs, it will have mounted the source tree and will provide a # prompt

At this prompt, an ls reveals...you should be positioned at /var/jargon, and see the source tree

```
# pwd
/var/jargon
# ls
CHANGELOG.md  docker-build.sh	    Dockerfile		   DOCKERTEST.md	   jargon-core	      jargon-mdquery  jargon-ruleservice  jargon-user-tagging  LICENSE.txt  README.md	  target
data-profile  docker-build-test.sh  docker-test-framework  eclipse-formatting.xml  jargon-data-utils  jargon-pool     jargon-ticket	  jargon-zipservice    pom.xml	    settings.xml  user-guide
# 

```

Note the settings.xml file is mounted that has the correct coordinates for the iRODS grid pre-configured with test accounts, resources, groups, etc as expected by the Jargon unit test framework.

Two scripts are provided, docker-build.sh does a mvn install -DskipTests, while ./docker-build-test.sh will run a mvn install, build the codebase, and then run the unit test framework. .jar files and test results are then available on the host machine in the source tree under the expected target directories in each Jargon sub-project.