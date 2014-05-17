package etcd

object Main extends App {
  val client = new EtcdClient("http://10.3.250.44:4001")

//  client.set("test", "testValue")
  client.get("test")
//  client.shutdown()
}