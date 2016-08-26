scala-etcd
==========

A simple scala client library for [etcd]

Uses spray http client to implement everything in a none blocking manner. It implements most of v2 api and returns objects to
represent the json.


```Scala

  val system = ActorSystem("etcd")
  val client = new EtcdClient("http://localhost:4001")(system)

  client.setKey("configKey", "configValue")

  val response: Future[EtcdResponse] = client.getKey("configKey")

  response onComplete {
    case Success(response: EtcdResponse) =>
      System.out.println(response)
      system.shutdown()
    case Failure(error) =>
      System.out.println(error)
      system.shutdown()
  }
```

This library is avilable in maven central and is cross compiled for 2.10 and 2.11

```XML

<dependency>
	<groupId>net.nikore.etcd</groupId>
	<artifactId>scala-etcd_2.11</artifactId>
	<version>0.8</version>
</dependency>
```

or

```XML


<dependency>
	<groupId>net.nikore.etcd</groupId>
	<artifactId>scala-etcd_2.10</artifactId>
	<version>0.8</version>
</dependency>
```

you can see here for all versions: http://repo1.maven.org/maven2/net/nikore/etcd/

[etcd]: http://coreos.com/blog/distributed-configuration-with-etcd/
