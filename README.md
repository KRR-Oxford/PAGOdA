PAGOdA
=====

PAGOdA is a conjunctive query answering engine for OWL 2 ontologies. The instruction how to use it can be found on the following website: http://www.cs.ox.ac.uk/isg/tools/PAGOdA/. 


Project dependencies/requirements
=====

- JavaSE 8
- Maven
- Eclipse Luna


How to compile the project from source in Eclipse?
=====

- In order to compile the project make sure that you have JavaSE 8 and Eclipse Luna installed on your computer.
- After cloning the repository by command git clone https://github.com/yujiaoz/PAGOdA.git, you need to load the project into Eclipse and convert it into Maven project.
- The project depends on a library JRDFox.jar that is not in central maven repository. The library for different operating systems can be found in lib directory. You need to include the correct one for your computer and add it to the build path. Alternative, if you have maven installed on your computer, you could add it into your local maven repository, modify maven dependencies and make sure that you remove it from the build path. If the provided JRDFox.jar doesn't work, you might need to follow the instruction below to compile the project JRDFox by yourself. 


How to compile RDFox?
===

- Download https://github.com/yujiaoz/PAGOdA/blob/master/lib/RDFox/RDFox.zip.
- Extract all files in the zip.
- Go to the directory RDFox/RDFox.
- Run command "ant". 
- You will find JRDFox.jar in the diretory build/release/lib.


How to run the project?
===

To run PAGOdA inside your java project, please check the simple example in src/example/simpleExample.jar. To run PAGOdA in command line once you have a jar package, follow the instruction on the following website https://www.cs.ox.ac.uk/isg/tools/PAGOdA/. 