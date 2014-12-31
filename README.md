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
The following settings are available in configuration file.

\# Should SOSWE try to create or join a cluster in current network.  
clustering=TRUE|FALSE   

\# Name of the cluster to create or join. There can be multiple clusters in the same network.  
clustername=soswecluster   

\# The port into which SOSWE will try to bind itself.
Port=9300   

\# Root url for REST calls. E.g. http://host:port/urlroot/restcall  
urlroot=soswe  

\# 'default' or the absolute path to xml workflow configuration. Default uses SOSWE_HOME/config/.xml.  
msconfigfolder=default      

\# 'default' or the absolute path to directory containing the executables which are used by the microservices.  
msexecutables=default    


