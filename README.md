scala-etcd
==========

A simple scala client library for [etcd]

Uses spray http client to implement everything in a none blocking manner. It implements most of v2 api and returns objects to
represent the json.


```Scala

  val client = new EtcdClient("http://localhost:4001")

  client.setKey("configKey", configValue")

  val response: Future[EtcdResponse] = client.getKey("configKey")

  response onComplete {
    case Success(response: EtcdResponse) =>
      System.out.println(response)
      client.shutdown()
    case Failure(error) =>
      System.out.println(error)
      client.shutdown()
  }
```

[etcd]: http://coreos.com/blog/distributed-configuration-with-etcd/
