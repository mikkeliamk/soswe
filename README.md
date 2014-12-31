SOSWE
=====
SOSWE is a Simple Open Source Workflow Engine for running micro-services based workflows.

Current version is a development build and has a lot of experimental and incomplete features. 

Background
---
SOSWE was built as part of OSA - Open Source Archive project as a bachelor thesis by Heikki Kurhinen. OSA is also released as open source by Mikkeli University of Applied Sciences.

Contact
--
Email  osa@mamk.fi  
Twitter  @OSArchive  
Web  http://osa.mamk.fi  
  
Mikkeli University of Applied Sciences

Contributors
--
**Developers**  
Heikki Kurhinen  
Liisa Uosukainen

License
---
The software is licensed under AGPL-3.0.  
All attached documentation is licensed under CC-BY-SA 4.0.  


Getting Started
---
For the most use cases the default settings will be fine.

###Installation
Download the source and compile. SOSWE has a built-in web container and doesn't require any external.

###Starting the instance
Run start on background, or run java -jar SOSWE.jar after compiling the source.

###Stopping the instance
There is few problems in stopping nodes. Current launch script cannot stop instance once it's started so user has to check the process id and kill the server process with `kill -9 {PID}`.

###Configuration
Currently there are following settings available in configuration file:  
clustering=TRUE|FALSE   # Should SOSWE try to form a cluster in current network?  

clustername=soswecluster   # What is the clusters name this node is part of, useful if there is more than one cluster working in the same network.  

Port=9300   # Port which SOSWE will try to bind itself to

urlroot=soswe    # Root of url for REST calls so its like http://host:port/urlroot/restcall  
msconfigfolder=default    #”default” or absolute path to xml file specifying the workflows if
“default” is used then workflows.xml from SOSWE_HOME/config/ will be used.  
msexecutables=default    #”default” or absolute path to folder containing different executables which are used by microservices. Developer can of course specify path inside each microservice but this way system becomes less portable. Microservice specific settings are always done by developer and they should be provided as
parameters in workflow configuration.  


