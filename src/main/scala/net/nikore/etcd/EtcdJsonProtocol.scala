package net.nikore.etcd

import spray.json._

object EtcdJsonProtocol extends DefaultJsonProtocol {

  //single key/values
  case class NodeResponse(key: String, value: Option[String], modifiedIndex: Int, createdIndex: Int)
  case class EtcdResponse(action: String, node: NodeResponse, prevNode: Option[NodeResponse])

  //for hanlding dirs
  case class NodeListResponse(key: String, dir: Boolean, nodes: Option[List[NodeResponse]])
  case class EtcdListResponse(action: String, node: NodeListResponse)

  //for handling error messages
  case class Error(errorCode: Int, message: String, cause: String, index: Int)

  implicit val nodeResponseFormat = jsonFormat4(NodeResponse)
  implicit val etcdResponseFormat = jsonFormat3(EtcdResponse)

  implicit val nodeListResponseFormat = jsonFormat3(NodeListResponse)
  implicit val etcdResponseListFormat = jsonFormat2(EtcdListResponse)

  implicit val errorFormat = jsonFormat4(Error)
}
