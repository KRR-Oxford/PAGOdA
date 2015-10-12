PAGOdA
======

PAGOdA is a conjunctive query answering engine for OWL 2 ontologies.


Project dependencies/requirements
=================================

- This software has been developed for Linux
- JavaSE 8
- Maven


Building and packaging
======================

This is a Maven project, hence Maven should be used for building and packaging.
* Compile `mvn compile`
* Create jars `mvn package`; 
you will get a jar without dependencies *target/pagoda-x.y.z.jar* and a runnable jar with dependencies *target/pagoda-x.y.z-jar-with-dependencies.jar*.
* Execute tests `mvn test -DskipTests=false`;


Configuring the engine
======================

The engine provides a standard configuration, but it is most likely that is not suitable for your purposes.
You should provide your own configuration in a file called `pagoda.properties` including it in the classpath.
Such a file should resemble the file `src/resources/_default_pagoda.properties`.


Executing
=========

Once you have the runnable jar, you can make the system answer queries through the command line interface.

Run `java -jar pagoda-x.y.z-jar-with-dependencies.jar` for getting a list of the mandatory arguments and optional parameters.

Run `java -cp pagoda-x.y.z-jar-with-dependencies.jar:<your_path_to_properties>/pagoda.properties arg [arg]...` for executing the engine with a custom configuration.

Using it as a library
=====================

The API is offered by the class `QueryReasoner`, however we provide a more handy wrapper, that is the class `Pagoda`.
Thanks to such a wrapper, you can build an instance of `Pagoda` by specifying different parameters and after that you will get
a runnable `Pagoda` object.
