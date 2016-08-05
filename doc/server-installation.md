# Hardware Requirements

At least 2 GB of RAM is recommended for running the server, together with 2 GB of disk space.

To process the background hierarchy, 8 GB of RAM and about 100 GB of disk space is required. However, the processing can be done on a different machine and the resulting hierarchy copied to the server.

In both cases, a fast recent CPU is recommended.

# Software prerequisites

A Java SE Development Kit (JDK), containing the Java Runtime Environment and development tools, is required. It is recommended to use Oracle JDK version 8 or later.

On top of that, SBT (http://www.scala-sbt.org/) is required to be installed.

Last, the bin folders of the JDK and the SBT installations have to be in system path, so that the sbt and javac commands can be run directly from a command line.

The application should be OS-independent, and has been verified to run on GNU/Linux and Microsoft Windows.

# Downloading Scaffold Visualizer

The Scaffold Visualizer can be obtained from GitHub, either in a form of a ZIP package, or using a GIT command:

git clone https://github.com/velkoborsky/scaffvis.git

In any case, after cloning from GIT or extracting the ZIP package, one should have a scaffvis folder containing the build 
file (build.sbt) and the project structure (folders client, generator, server,…).

The folder does not yet contain the required libraries. Most of the libraries are downloaded from the Internet automatically, some require to be downloaded manually, as described in the following section.

# Downloading Libraries

The Scaffold Visualizer requires ChemAxon JChem cheminformatics toolkit to run. It is not required to install the 
toolkit. Instead, it is needed to copy a few selected libraries from the distribution ZIP file.

## Step 1: Downloading the JChem toolkit

At the time of writing, the ChemAxon JChem Suite can be downloaded from:

https://www.chemaxon.com/download/jchem-suite/

It is recommended to select the platform independent ZIP file option - a “Cross platform package without installer”. The 
Scaffold Visualizer has been developed and tested with JChem Suite version 16.1.4.0 (from Jan 5, 2016), however, it might be possible to use a later version. The version 16.1.4.0 is available from Archives at the bottom of JChem Suite download page, or directly at:

https://www.chemaxon.com/download.php?d=/data/download/jchem/16.1.4.0/

In any case, at the end of this step you should have a JChem package file such as jchem-16.1.4.0.zip or an equivalent later version.

## Step 2: Extracting the libraries

The JChem package file should contain binary libraries in the jchem/lib/ subdirectory. From all the libraries contained there, only a small subset is required. The exact list of the required libraries is presented in file generator/lib/REQUIRED_LIBRARIES.txt of the Scaffold Visualizer distribution.

It is important to copy only the libraries prescribed. Simplifying the task and copying all the libraries that are provided with ChemAxon JChem could lead to classpath conflicts with the libraries used by the Play Framework – i.e. the 
generator would still work fine but the server might not work at all.