Installing JAI

JAI (Java Advanced Imaging) is used in jargon-data-utils for thumbnail processing in the local or server JVM.  Unfortunately, JAI is not distributable
via Maven, so it needs to be installed manually into your Maven repository.  Follow these steps to get around the issue of 
missing JAI artifacts:

1) go to http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-client-419417.html#jai-1_1_2_01-oth-JPR
2) download the JAI jar files, on Mac, I grab the Linux CLASSPATH intall,  Unizip these
3) download jai_imaageio from http://download.java.net/media/jai-imageio/builds/release/1_1/ (I grab the jar.zip file)
4) for the respective jar files, run the jai_install.sh (you need to edit it to reflect the path you installed to)

Note, it's easy to just use the command line to cd to the directory where the correct jar file is located and just cut/paste the mvn install
command there.  This places the artifact in your maven repository and then proceeds as normal.  
