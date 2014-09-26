package net.nikore.etcd;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class JavaEtcdClientSmokeTest {
  public static void main(String ... args) throws Exception {
    EtcdClient client = new EtcdClient("http://localhost:4001");

//    Future<EtcdJsonProtocol.EtcdResponse> setResponse = client.setKey("test1", "testValue");
//
//    EtcdJsonProtocol.EtcdResponse etcdSetResponse = Await.result(setResponse, Duration.create(5, TimeUnit.SECONDS));
//
//    System.out.println(etcdSetResponse);

    Future<EtcdJsonProtocol.EtcdResponse> getResponse = client.getKey("test4");

    EtcdJsonProtocol.EtcdResponse etcdResponse = Await.result(getResponse, Duration.create(5, TimeUnit.SECONDS));

    System.out.println(etcdResponse);
  }
}
