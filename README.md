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


### Creating a workflow
Creating workflows is done by modifying the workflow configuration xml. Below is a small example.

```xml
<workflow name="Batch-ingest" defaultAction="yes" description="Moving files to workspace" >
  <!--
  Name: this attribute is the name of this workflow and it is used to call the workflow from REST-api.
  defaultAction: use value “yes” for now, this is not properly implemented yet..
  description: short description about what does your workflow do? Available thru REST-api.
  -->
  <options>
    <mongoHost option="localhost" />
    <mongoPort option="27017" />
    <mongoDBName option="soswe" />
  </options>
  <!--
  All static parameters for microservices should be provided like this, these options are available to
  all microservices in map like {“mongoHost” : “localhost”} and so on.
  -->
  <task name="CleanCheck" important="true" />
  <!--
  name: name of the microservice which should be called here, this needs to be exactly the same as
  the microservice return in it's capabilities method, see creating microservices chapter.
  Important: should all microservices after this one be cancelled if this one fails.
  -->
  <task name="ReadMetadata" important="true" />
  <task name="SaveToMongo" important="true" />
</workflow>
```

### Creating a microservice
Creating a microservice requires creating two files: a Java class and an xml description. XML description needs to be saved under `SOSWE_HOME/microservices/microservice_config/`

```xml
<service name="SaveToMongo">
  <!-- name: this needs to be exatly the same as in java classes capabilities method and in workflow
  configuration file -->
  <description>Save metadata to mongoDB</description>
  <!-- Short description -->
  <exec>nothing</exec>
  <!-- if you want to run other jars or executables provide a path here. If you want to keep your
  system portable use string like {pathToMicroservices} and replace it with
  Osa.microserviceJarFolder in <microservice>.java
  -->
  <log>/var/log/soswe/microservices/SaveToMongo/</log>
  <!-- <date>.log is created into this dir -->
</service>
```
The class should implement an interface but to make coding easy and fun there is a premade microservice abstract class to be extended. Just use soswe.jar as a dependency when creating your microservices.

Plugin class needs to be annotated with `@PluginImplementation` annotation.

Note that the first method needs to to annotated with `@Capabilities` and the name property needs to be exactly the same as in microservice xml file and in workflow configuration xml file.

```java
@Capabilities
  public String[] caps() {
    return new String[] {"name:Ping"};
}
```
Parameters:
* input = All the output from the previous microservice.
* Options = A HashMap containing all options specified in workflow config xml. All the dynamic launch options given via the REST API and possible values set by the previous micro-services in the workflow.

Currently, there is no implementation to monitor the progress of a microservice but you should keep the state variable updated during its execution. Possible value are `waiting|running|error|completed`.

When you get the output set super's output variable to it. 

In the end of your microservice run `log();` method to create a log file.

Once the microservice is finished you should use super's `setCompteled(true)` method to tell that it's now finished. This should be done even when there may be an error. In this case just set the state accordingly.

```java
@Override
public boolean execute(String input, HashMap<String, Object> options) throws Exception {
  super.setState("running");
  if(input != null && !input.isEmpty()){
    //Handle input from previous microservice here
  }
  //HashMap options contains launch options specified in xml conf
  boolean success = false;
  String output = "";
  Process p;
  try {
    p = Runtime.getRuntime().exec(super.getExec());
    p.waitFor();
    BufferedReader reader = new BufferedReader(new
    InputStreamReader(p.getInputStream()));
    String line = reader.readLine();
    if(line != null){
      output += line+"\n";
    }
    while (line != null) {
    line = reader.readLine();
      if(line != null){
        output += line+"\n";
      }
    }
    /*
    * to really know if the command did what you wanted it to do you need to analyze output here and determine if
    * it is correct. Otherwise UI may display that execution succeeded for example if ping command was successful
    * but there was no answer.
    */
    success = true;
    super.setState("completed");
    super.setOutput(output);
    super.setCompleted(true);
  } catch (Exception e) {
    e.printStackTrace();
    success = false;
    super.setOutput(e.toString());
    super.setState("error");
    super.setCompleted(true);
  }
  log();
  return success;
}
```
