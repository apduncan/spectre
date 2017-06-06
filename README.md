![alt text](spectre.png "Suite of Phylogenetic Tools for Reticulate Evolution")

Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
==============================================================

The aim of this project was to create a single project that contains a number of reusable tools for modelling and
visualising reticulate evolution via phylogenetic trees and networks.  It contains a number of previous published tools 
for generating phylogenetic networks for various types from a range of different inputs.  SPECTRE also contains a library 
containing data structures and algorithms that can be leveraged by third party applications.


Installing
==========

Spectre can be installed either from a distributable tarball, or from source via a `git clone`. These steps for both
methods are described in the following sections.

Some of the tools in spectre use external mathematical optimizers for solving linear and quadratic problems.  Should you
install from tarball working version of Apache Maths and JOptimizer is included.  However, some users may want to use
optimizers from other vendors or sources such as GLPK or Gurobi you will need to manually install another tool called
*metaopt* first, and then install from source.  Metaopt can be obtained from https://github.com/maplesond/metaopt.  Please
follow the instructions in the metaopt README for how to add other optimizers.


From tarball
------------

Before starting the installation please ensure that the Java Runtime Environment (JRE) V1.8+ is installed and configured
for your environment.  You can check this by typing the following at the command line: ``java -version``.  Double check
the version number exceeds V1.8.

The installation process from tarball is simple.  The first step is acquire the tarball from https://github.com/maplesond/spectre/releases.
Then unpacking the compressed tarball to a directory of your choice.  The unpack command is: ``tar -xvf spectre-<version>-<platform>.tar.gz``.
This will create a sub-directory called ``spectre-<version>`` and in there should be the following further sub-directories:

* bin - contains scripts allowing the user to easily run all the tools.  The scripts are all command line tools except for ``spectre``, which provides an interactive graphical interface.  Scripts for all platforms are available, in general, those with no extension should work on linux and mac platforms, and those with a ``.bat`` extension should run on windows.
* doc - a html, pdf and text copy of the complete manual
* etc - contains examples and configuration files
* examples - Example files to help you get started with the spectre tools
* repo - contains the java classes used by spectre
* support_jars - contains source and javadocs for the spectre codebase

Should you want to run the tools without referring to their paths, you should ensure the `bin` directory is on your
PATH environment variable.

Unfortunately, as we cannot bundle Gurobi resources with SPECTRE, if you wish to enable Gurobi support you must install SPECTRE
from source (see below).

From source
-----------

Spectre is a java 1.8 / maven project. Before compiling the source code, please make sure the following tools are installed:

* GIT
* Maven (make sure you set the m2_home environment variable to point at your Maven directory) https://maven.apache.org/
* JDK v1.8+  (make sure you set the JAVA_HOME environment variable to point at your JDK directory)
* Make
* Sphinx (may require you to install python, also make sure the sphinx-build is on the path environment variable) http://www.sphinx-doc.org/en/stable/

You also need to make sure that the system to are compiling on has internet access, as it will try to automatically
incorporate any required java dependencies via maven. Because spectre is a maven project, almost all the other
dependencies (not mentioned here) will be downloaded automatically
as part of the Maven buildcycle.  However, the one exception to this is a java library called metaopt (described at the
beginning of this section), which provides a common interface to several open source and commercial optimizers.  Metaopt
can be obtained from: https://github.com/maplesond/metaopt. Please follow the instructions in the metaopt README and
make sure the metaopt library has been added to your local maven repository.  After this, you can proceed with the
spectre installation.

Now type the following::

    git clone https://github.com/maplesond/spectre.git
    cd spectre

Then type::

    mvn clean install

or, if you wish to enable gurobi optimizer support::

    mvn clean install -P gurobi

Note: If you cannot clone the git repositories using the ``https`` protocol, please try ``ssh`` instead. Consult github to obtain the
specific URLs.

Assuming there were no compilation errors. The build, hopefully the same as that described in the previous section, can
now be found in ``./build/spectre-<version>``. There should also be a dist sub directory which will contain a tarball
suitable for installing spectre on other systems.


Core Library
============

Contains classes that are used by other modules, that contain some kind of general functionality which means they can be
used in different situations.  These classes were broken down into sub groups based on their specific kind of
functionality as follows:

* ds - Data structures - Commonly used phylogenetic data structures relating to concepts such as: Splits, Trees, Networks, Distances and Quartets
* io - Input and Output - Classes that help loading and saving common phylogenetic file formats.  Specifically, Nexus and Phylip format.
* math - Maths - Classes related to common mathematical data structures and algorithms such as basic statistics, matrix algebra, and storing of tuples.
* ui - User interface - Supporting classes to help with both command line interfaces and graphical user interfaces
* util - Miscellaneous utilities - Anything we might conceivably want to reuse that doesn’t fit elsewhere.

Core is designed to contain most of the core functionality of the tools within spectre.  The idea being that other
developers can design there own tools whilst leveraging the functionality in this library.


Tools
=====

SPECTRE contains the following tools, which are accessible to the user via graphical and command line interfaces.

 - FlatNJ - Constructs a flat split network from a multiple sequence alignment, weighted quartet data or location data.
 - Netmake - An implementation of NeighborNet and other variants, for rapidly constructing a circular split network from a distance matrix or a sequence alignment.
 - NetME - Constructs a minimum evolution tree compatible with an existing circular split network, such as created by NeighborNet.
 - SuperQ - Constructs a circular split network from a set of (partial) input trees.

The graphical interface is invoked via the `spectre` script and can visualize the 
planar split networks output from the tools.


Quick Start:
============

Assuming the user has access to the compiled executable jars for spectre, then they should only need JRE 1.7+ installed
on their system.  The tools can be found in the bin subfolder.


Further Documentation
=====================

The full manual can be found in the ``doc`` directory, or online at: http://spectre-suite-of-phylogenetic-tools-for-reticulate-evolution.readthedocs.io/en/latest/


Issues
======

Should you discover any issues with spectre, or wish to request a new feature please raise a ticket at https://github.com/maplesond/spectre/issues.
Alternatively, contact Daniel Mapleson at: daniel.mapleson@earlham.ac.uk


Availability:
=============

Open source code available on github: https://github.com/maplesond/spectre.git

License: GPL v3


Contact
=======

* Daniel Mapleson - Earlham Institute (EI)
* Sarah Bastkowski - Earlham Institute (EI)
* Taoyang Wu - University of East Anglia (UEA)
* Andreas Spillner - Universitat de Griefswald
* Vincent Moulton - University of East Anglia (UEA)
