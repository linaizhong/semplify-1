package modules.graphql.services

import com.google.inject.ImplementedBy
import modules.entityhub.models.{GraphGet, IRI, Literal, Triple, SearchHit}
import modules.fileserver.models.FileInfo
import modules.graphql.services.impl.GraphQLServiceImpl
import modules.project.models.ProjectGet
import modules.sparql.models.QueryGet
import modules.task.models.TaskGet
import modules.webcrawler.models.PageGet

import scala.concurrent.Future

@ImplementedBy(classOf[GraphQLServiceImpl])
trait GraphQLService {
  def node(projectId: String, graph: Option[String], uri: String): Future[Option[IRI]]

  def prefLabel(projectId: String, uri: String): Future[Option[Literal]]

  def depiction(projectId: String, uri: String): Future[Option[IRI]]

  def triplesFromNode(projectId: String, graph: Option[String], from: String, nodeType: Option[String]): Future[Seq[Triple]]

  def triplesToNode(projectId: String, graph: Option[String], to: String): Future[Seq[Triple]]

  def projects(): Future[Seq[ProjectGet]]

  def tasks(): Future[Seq[TaskGet]]

  def searchNodes(projectId: String, graph: Option[String], term: String): Future[Seq[SearchHit]]

  def crawledPages(projectId: String): Future[Seq[PageGet]]

  def files(projectId: String): Future[Seq[FileInfo]]

  def sparqlQueries(projectId: String): Future[Seq[QueryGet]]

  def graphs(projectId: String): Future[Seq[GraphGet]]

  def deleteGraphs(projectId: String, graphs: Seq[String]): Future[Seq[GraphGet]]

  def deleteFiles(projectId: String, fileIds: Seq[String]): Future[Seq[FileInfo]]
}