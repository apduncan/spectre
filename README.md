![alt text](spectre.png "Suite of Phylogenetic Tools for Reticulate Evolution")

Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
==============================================================

The aim of this project was to create a single project that contains a number of reusable tools for modelling and
visualising reticulate evolution via phylogenetic trees and networks.  It contains a number of previous published tools 
for generating phylogenetic networks for various types from a range of different inputs.  SPECTRE also contains a library 
containing data structures and algorithms that can be leveraged by third party applications.


Installing
==========

SPECTRE can be installed via three main methods either from a platform-specific install, pre-packaged tarball, or directly
from github source repository via a `git clone`. The necessary steps for all methods are described in the following sections.

Some of the tools in SPECTRE use external mathematical optimizers for solving linear and quadratic problems.  Should you
install from a platform-specific installer or cross-platform tarball then a working version of Apache Maths and JOptimizer
is included.  However, some users may want to use optimizers from other vendors or sources such as Gurobi. In this case
you will need to install another tool called *metaopt* first, and then install from source.  Metaopt can be obtained
from https://github.com/maplesond/metaopt.  Please follow the instructions in the ``metaopt`` README for how to add
other optimizers.  Then follow the instructions for installing from source below.

Platform-specific installer
---------------------------

SPECTRE currently supports Debian/Ubuntu, MacOS and windows installers.  Users of these platforms should find the installation
experience self-explanatory.  They should only need to download the appropriate file from the github repository releases
page: https://github.com/maplesond/spectre/releases and then double click the downloaded file.  There are however, some
platform-specific considerations for running SPECTRE which are detailed below.

*Debian/Ubuntu*

Installing the debian file will put a shortcut for the GUI into either your ``Science`` or ``Other`` menu section depending
on how you have your system configured.  Links to the command-line versions of your apps will be added to ``/usr/bin`` and
the program itself is installed to ``/usr/share/spectre``.

*MacOS*

After double-clicking the DMG image file, drag the SPECTRE app into the Applications folder.  You should then be able to
access the GUI from the launchpad.

On Mac platforms, the application may be regarded as a software from unidentified developers and hence they may need to open it as following:


1.	Open a new Finder Window
2.	Navigate to the Download or Application you just installed to your Mac
3.	Instead of Double-Clicking on the Icon to open, Right Click on it and choose "Open"
4.	A Pop-Up Window will appear telling you that the Software is from an Unidentified Developer, but it will give you the option to Open it anyway. Click on "Open", and the software will run as expected.


https://support.novationmusic.com/hc/en-gb/articles/207561205-My-Software-is-from-an-Unidentified-Developer-and-won-t-Install-on-my-Mac


We have not at present installed the command-line tools onto the PATH, but they are present on your system and can be found
in ``/Applications/Spectre.app/Contents/MacOS``.  You can either run them from here or manually link them into ``/usr/local/bin``
in order to have them directly available from the terminal.

*Windows*

The windows installer will allow you to install SPECTRE to a directory of your choosing.  After which it should be available
from the start menu.

Please note the command-line versions of the tools are not available on windows via this method.


Pre-packaged Tarball
--------------------

Before starting the installation please ensure that the Java Runtime Environment (JRE) V1.8+ is installed and configured
for your environment.  You can check this by typing the following at the command line: ``java -version``.  Double check
the version number exceeds V1.8.

The installation process from tarball is simple.  The first step is acquire the tarball from https://github.com/maplesond/spectre/releases.
Then unpacking the compressed tarball to a directory of your choice.  The unpack command is: ``tar -xvf spectre-<version>-<platform>.tar.gz``.
This will create a sub-directory called ``spectre-<version>`` and in there should be the following further sub-directories:

* bin - contains scripts allowing the user to easily run all the tools.  In general, the scripts are all command line tools except for ``spectre`` suffix.  Scripts for all platforms are available, in general, those with no extension should work on linux and mac platforms, and those with a ``.bat`` extension should run on windows.
* doc - a html, pdf and text copy of the complete manual
* etc - contains configuration files and other resources for the application
* examples - Example files to help you get started with the SPECTRE tools
* repo - contains the java classes used by SPECTRE

Should you want to run the tools without referring to their paths, you should ensure the `bin` directory is on your
PATH environment variable.


From source
-----------

SPECTRE is a java 1.8 / maven project. Before compiling the source code, please make sure the following tools are installed:

* GIT
* Maven (make sure you set the m2_home environment variable to point at your Maven directory) https://maven.apache.org/
* JDK v1.8+  (make sure you set the JAVA_HOME environment variable to point at your JDK directory)
* Make
* Sphinx (may require you to install python, also make sure the sphinx-build is on the path environment variable) http://www.sphinx-doc.org/en/stable/

You also need to make sure that the system you are compiling on has internet access, as it will try to automatically
incorporate any required java dependencies via maven. Because SPECTRE is a maven project, almost all the other
dependencies (not mentioned here) will be downloaded automatically
as part of the Maven buildcycle.  However, the one exception to this is a java library called metaopt (described at the
beginning of this section), which provides a common interface to several open source and commercial optimizers.  Metaopt
can be obtained from: https://github.com/maplesond/metaopt. Please follow the instructions in the metaopt README and
make sure the metaopt library has been added to your local maven repository.  After this, you can proceed with the
SPECTRE installation.

Now type the following::

  git clone https://github.com/maplesond/spectre.git
  cd spectre

Then type::

    mvn clean install

or, if you wish to enable gurobi optimizer support::

    mvn clean install -P gurobi


Note: If you cannot clone the git repositories using “https”, please try “ssh” instead. Consult github to obtain the
specific URLs.

Assuming there were no compilation errors. The build, hopefully the same as that described in the previous section, can
now be found in ./build/spectre-<version>. There should also be a dist sub directory which will contain a tarball suitable
for installing SPECTRE on other systems.


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

Core is designed to contain most of the core functionality of the tools within SPECTRE.  The idea being that other
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

Assuming the user has access to the compiled executable jars for SPECTRE, then they should only need JRE 1.8+ installed
on their system.  The tools can be found in the bin subfolder.


Further Documentation and Citing
================================

The full manual can be found in the ``doc`` directory, or online at: http://spectre-suite-of-phylogenetic-tools-for-reticulate-evolution.readthedocs.io/en/latest/

We also have a preprint available on BioRxiv: http://www.biorxiv.org/content/early/2017/07/27/169177.  Should you use our software please cite this paper.



Issues
======

Should you discover any issues with SPECTRE, or wish to request a new feature please raise a ticket at https://github.com/maplesond/spectre/issues.
Alternatively, contact Sarah Bastkowski at: sarah.bastkowski@earlham.ac.uk, or Daniel Mapleson at: daniel.mapleson@earlham.ac.uk.


Availability:
=============

Open source code available on github: https://github.com/maplesond/spectre.git

License: GPL v3


Contact
=======

* Sarah Bastkowski - Earlham Institute (EI)
* Daniel Mapleson - Earlham Institute (EI)
* Taoyang Wu - University of East Anglia (UEA)
* Andreas Spillner - Merseburg University of Applied Sciences
* Vincent Moulton - University of East Anglia (UEA)
