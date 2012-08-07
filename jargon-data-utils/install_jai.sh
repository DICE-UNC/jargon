# example install commands for the JAI jars, see the README-JAI.txt file in this project for explanation
mvn install:install-file -Dfile=jai_core.jar -DgroupId=javax.media -DartifactId=jai_core -Dversion=1.1.2_01 -Dpackaging=jar
mvn install:install-file -Dfile=jai_codec.jar -DgroupId=javax.media -DartifactId=jai_codec -Dversion=1.1.2_01 -Dpackaging=jar
mvn install:install-file -Dfile=jai_imageio.jar -DgroupId=com.sun.media -DartifactId=jai_imageio -Dversion=1.1 -Dpackaging=jar