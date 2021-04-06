# 9X - Pasta

Is a suite of tools and libraries inspired around the 9P and Styx protocols, this allows for
building distributed applications.

Inspired because it doesn't quite at the moment follow the conventions of the protocol strictly. My
aim is for compatability with 9P but extend it (hence the X) so that is less focused on the Plan 9
file system. For example some extentions can be the time fields - to 64 bits. Better version
information in a more structured way, as well as capability management with out 'modifying' the main
9P messages - So extend the number of messages not the messages themselves.

The library is still in development.

## Protocol

This is a low level library, which converts the messages to a byte stream, it depends on a seperate
library to transport the bytes to the target. It consists of an Encoder/Decoder pair.

## Gelato

Is a library that uses the 'protocol' package, it uses TCP/IP as the transport medium to move the
messages between users. The library is split in two parts, Client and Server.

### Client

From a client perspective Gelato offers two ways to interact with a server, one is a futures API the
other is more managed API where the objects of operation are directories (including InputStream and
OutputStream).

#### GelatoMessaging

The messaging API is quick to use, the main object is the GelatoMessage object, a wrapper which is
built around a pair of messages transactions ie: <Request,Response>.

For Example:

```
GelatoMessaging messaging = new GelatoMessaging(hostName, portNumber);
GelatoMessage<VersionRequest,VersionRequest> versionRequest = messaging.createVersionRequest();
messaging.submitMessage(versionRequest);
VersionRequest rspVersion = versionRequest.getMessage();
messaging.close(versionRequest);
```

1. Instantiate a connection, specifying the host and port number on the GelatoMessaging constructor
2. Create a Transaction, in the example a VersionRequest pair - the GelatoMessaging class provides
   templates
3. Submit the message, ie: send the mesage
4. We can then get the response message by calling the futures getMessage function
5. Close the message - Since the future is held in reserve by the library once use is finished it
   must be notified that it is closed, otherwise you will have a memory leak
6. Alternatively if you dont care about the response - submitAndClose is available - however you
   can't get the response, and if there is an error it will throw a Runtime Exception.

#### GelatoFileManager

This API uses the GelatoMessaging API to build more of a managed suite of objects. These basically
represent files and directories. The main class is GelatoFileManager, which takes as its input on
the constructor host, port, and a user identifier.

```
GelatoFileManager fileManager = new GelatoFileManager(server,port, user);
GelatoDirectory root = fileManager.getRoot(); // Get the root of the server
List<GelatoDirectory> directories = root.getDirectories();
List<GelatoFile> files = root.getFiles();

//To only select a single file

GelatoFile exampleText = root.getFile("example.txt");
InputStream inputStream = exampleText.getFileInputStream();

//To only select a single Directory
GelatoDirectory exampleDir = root.getDirectory("Example");

```

### Server

The server suite of API's is more locked down, there is only really one way to use it, that is via
the GelatoServerManger object, while it is possible to get the individual messages from connections,
its not something really used.

The API is built on the concept of Controllers, that represent either files or directories, there is
also a 'resource' controller which is the most generic and doesn't specify the type of resource.

Each controller has a set of handlers, which it invokes to resolve the specific request type on that
resource, they have some built in handlers, but one can override them.

The Server Library is quite parallel, however it makes the following 'assumptions' - resources only
process one transaction at a time, that is resource cause request contention. There are options, as
seen in the ParallelHandlerMode, by default CONTENTION is used.

```
  public enum ParallelHandlerMode {
    CONTENTION,  //Resource handles requests in serial mode -
    ROUNDROBIN,  //Resources are paried with requests and then scheduled to be executed in a round robin mannger
    SESSION_CONTENTION //Resources are serialised only in a session, but otherwise run in parallel agaisnt other sessions
  } 
```

These modes impact how the GelatoParallelHandler schedules the tasks to be executed

```
   long key = 0;
    if(handlerMode == GelatoServerManager.ParallelHandlerMode.CONTENTION) {
      key = serverResource.getQid().getLongFileId();
    } else if (handlerMode == GelatoServerManager.ParallelHandlerMode.ROUNDROBIN){
      key = requestCount;
    } else if(handlerMode == GelatoServerManager.ParallelHandlerMode.SESSION_CONTENTION){
      key = descriptor.getDescriptorId() + serverResource.getQid().getLongFileId();
    }
    Ciotola.getInstance().execute(parallelRequest,key);
```

Gelato uses another library called Ciotola - it has a way of managing tasks via a
KeyPoolExecutioner, tasks which are serialised are pinned to a specific execution Group - determined
by the key. So resources are executed in parallel, File A,B,C get scheduled to different execution
pools. Transactions A1,B1, A2 would be executed slightly differently depending on the mode. On
contention A1,A2 would be executed on the same pool, but B1 can be different. In session contention
if A1 and A2 are from different client connections they can be potentially run on different queues.
Round Robin tries to maximise CPU usage by equally distributing tasks across all CPU's with out
consideration of transaction dependency.

Depending on how you want things to go, you have these options. For the most part CONTENTION is the
default as it provides some what sane assumptions, parallellism where possible but prevent multiple
resource access. This follows more closely to how a File system behaves.

Using the GelatoServerManager

```
 GelatoServerManager serverManager = new GelatoServerManager(port); //specify the listening port
 GelatoDirectoryController myRootDirectory = new MyRootDirectory(); //Example 
 GelatoFileController myFile = new MyFancyFile();
 myRootDirectory.addFile(myFile);
 serverManager.setRootDirectory(myRootDirectory);
 serverManager.start();
```

But how do you make a GelatoDirectoryController??? or event a File - This is where the Server API
gets a bit complicated, and what I want to make easier in Sorbet. However you can implement the
Interface, use GelatoDirectoryControllerImpl or extend GelatoDirectoryControlelrImpl

### Other Comments

Gelato Offers other capabilities - like Proxies, which allow you to pick up messages and re-route
them, this is used in Fettuccine when doing remote-mounted servers. It doesn't specify
Authentication, or verify permissions these are left as an exercise for the reader. One needs to use
the hooks available in the library to implement these.

## Agnolotti

This is an RPC libary built on top of Gelato it allows one to quickly build remote services. Again
the library is made of a client API and a Server API. The intention was to make a self descriptive
RPC mechanism, 9P/X is a file system that is available on the network. When a server is created it
exports this IDL which clients can read and then invoke the methods accordingly with out having to
involve intermediate compilers or interpreters.

### Server

To make a remote service is quite easy, one needs to create an Interface

```
public interface EchoDemo {
       String echo(String msg);
}
```

This is required as the interface is exported to services remotely, the client code is all
autogenerated using Dynamic Proxies during run time.

We now need a service on the server which implements this interface.

```
public class EchoDemoImpl implements  EchoDemo{

       @Override
       public String echo(String msg) {
            System.out.println(msg);
            return msg;
       }
}
```

Once the this is completd we now export the Interface and service with Agnolotti. To do this we use
the ServiceManager class,

```
   ServiceManager serviceManager = new ServiceManager("1.0",   //Version Descriptor
        "echoServices",  //The Service we are exporting 
         9092,  //Port Number
        "example",  //User owner
        "example"); //Group Owner
   serviceManager.addRemoteService(EchoDemo.class,new EchoDemoImpl());
```

The ServiceManager takes a String describing the version of the service, a Service name, we than add
one "Service" per interface, in this we only have one so we only call addRemoteService once.

### Client

The client side is equally 'easy' to use as said earlier on the client side dynamic proxies are used
rather than object stubbing. The server exports a file called idl.json which is read by the client
and generates the dynamic proxies based on that IDL.

On the client end you use the RemoteClient object, you connect it to the target system, and then use
getRemoteService to get the service you are after.

```
        RemoteClient client = new RemoteClient("localhost" , //hostname
                9092, //port
                "echoServices",  //Service we are after
                "1.0", //Version number
               "test"); //User we are connecting with

        EchoDemo demo = (EchoDemo) client.getRemoteService(EchoDemo.class); //Get the Remote Service - Cast to type
        System.out.println(demo.echo("hello")); //Call the service
```

### Closing Remarks

Compared with XML-RPC, Agnolotti doesn't require casting of values passed b/w client and server also
the Client sees this as a normal Interface so the conversion is handled for you by the library.

Compared with SOAP, it does not require the verbosity, or is as heavyweight, and much easier to use
the protocol it self allows for more powerful abstractions which are not available.

The library is still in development, more features are going to be added at the moment it behaves
more like an RPC library, not quite Object Oriented. This will change, also doesn't quite understand
non-primitive types however. The main idea with this is to have the Client objects described by the
server which can be autogenerated.

## Fettuccine

Is an Interposer/Virtual FS service, it acts as a service bus, this means one can wire services
built with Gelato to create distributed applications.

Since the 9P protocol describes a file system, Fettuccine is really the VFS layer, where one can
mount these services to Fettuccine, the intention is for Fettuccine to allow discovery and component
building, it will support concepts such as name spaces and routing.

Fettuccine is accompanied by FettuccineShell which is an application which can connect to 9P/X
servers, however it uses Agnolotti as a demonstration by providing the remote execution of various
Fettuccine command such as Mount.

