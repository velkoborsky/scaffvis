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

# Running Scaffold Visualizer

Now, everything should be ready to run the Scaffold Visualizer. In a command line, navigate to the folder where the Scaffold Visualizer is installed (i.e. the folder containing the build.sbt build file). You shall be able to start an interactive SBT console by executing the sbt command.

It is safe to start executing a sample generator task via command:

> generator/run CheckPaths

The task will check for various paths (such as the PubChem compound source directory and the database files) and report their existence and locations.

Now the generator can be used to generate the background hierarchy. Which can be used when a sample hierarchy is included (see the CheckPaths task output) or when providing a scaffold hierarchy (file scaffoldHierarchy.mapdb) generated elsewhere.

# Generating the background hierarchy

The background scaffold hierarchy is generated from PubChem files in three distinct steps (as described in the thesis). They can be run all at once, using asupplied SBT command:

> generate

Alternatively, the tasks can be run one by one. That can be done the same way as with the CheckPaths task or by changing to the generator project

> project generator

Then, tasks can be run using the run command only, for example:

> run ImportPubChem

As described earlier, the *ImportPubChem* task converts the PubChem Compound SDF representation to a custom format. Before running the ImportPubChem task, download the PubChem Compound database (see pubchem/README.txt) and check that it is correctly detected using the CheckPaths task. Running the ImportPubChem task, a PubChem database in a custom format shall be created in
hierarchy/pubchem.mapdb (see also hierarchy/README.txt for details on how to change the default path).

The second task, *GenerateScaffolds*, uses the PubChem database file generated in the first step and calculates a raw processing hierarchy, creating a file hierarchy/processingHierarchy.mapdb.

This file is then used in the third step *GenerateHierarchy* to create the final hierarchy, by default stored in hierarchy/scaffoldHierarchy.mapdb.

Having finished the generation, all files besides the result -  the scaffoldHierarchy.mapdb file - may now be safely
deleted. Also, please make sure, that the target database files do not exist prior to calling the processing task creating them – unless resuming an interrupted computation.

# Running the server in development mode
Having a hierarchy file ready, we can run the server. That can be done either in development mode, which is easy, or in a production mode, which is a bit more involved and will be described in the next section.

Running the server in the development mode consists of calling a single SBT command:

> server/run

That should immediately inform us, how the application can be accessed. By default, the server listens on address http://localhost:9000/.

Running the server might fail in case the background scaffold hierarchy is not available (see previous section on how to generate it and use the CheckPaths generator task to verify that it is correctly detected). The development server also fails with a hard to debug mistake when the javac Java compiler is not in system path environment variable.

The development mode can be used to run the server for the first time – to verify that everything works as expected. On top of that, the development mode is useful when customizing the application. The changes made to the source code are immediately applied to the running server, which allows for rapid iterations.

# Running the server in production mode

## Creating a distribution package

To run the server in, a binary distribution package needs to be created. In order to do that, switch to the server project:

> project server

Then, if creating a distribution package for the first time, generate a new cryptographic secret (consult [Play Framework documentation](https://www.playframework.com/documentation/2.5.x/ApplicationSecret) for more details):

> playUpdateSecret

Next you can generate a binary distribution package using the supplied release command:

> release

You shall be informed where to find the resulting package. By default it shall reside in a file server-1.0.0.zip, in folder server\target\universal.

## Running the server
The package server-1.0.0.zip then needs to be extracted to a suitable location and run from a command line using the included launchers in bin folder (bin/server for Linux and bin/server.bat for Windows).

The server can either be run from the computer where it has been compiled or copied to a dedicated server and run from there. The package is platform independent and only needs a server Java Runtime Environment (it doesn’t need the JDK nor the SBT).

The package also contains all the required libraries. It does not, however, contain the scaffold hierarchy (legal reasons - redistribution not permitted). This hierarchy has either be copied to the server (to the path hierarchy/scaffoldHierarchy.mapdb) or an alternative location has to be specified (as described in hierarchy/README.txt).

# More resources
The server is a standard Play Framework application and detailed information about preparing distribution packages and running Play applications in production can be found in the Play Framework documentation 2 as well as in the documentation of the SBT Native Packager.
