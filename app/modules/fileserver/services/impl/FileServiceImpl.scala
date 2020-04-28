package modules.fileserver.services.impl

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths

import akka.stream.Materializer
import javax.inject.Inject
import modules.fileserver.models.{File, FileInfo}
import modules.fileserver.services.FileService
import modules.project.services.ProjectService
import play.api.libs.Files
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.MultipartFormData
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.akkastream.GridFSStreams
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.bson.{BSON, BSONDocument, BSONObjectID}
import reactivemongo.api.gridfs.GridFS
import reactivemongo.bson.{BSONArray, BSONString, BSONValue}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import utils.BSONSerializationPackDocument._

class FileServiceImpl @Inject()(projectService: ProjectService,
                                reactiveMongoApi: ReactiveMongoApi)
                               (implicit ec: ExecutionContext, m: Materializer) extends FileService {

  val database: Future[DefaultDB] = reactiveMongoApi.database

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("fs.files"))

  override def save(file: MultipartFormData.FilePart[Files.TemporaryFile],
                    dataParts: Map[String, Seq[String]]): Future[FileInfo] = database flatMap {
    db => {
      val gridFS = GridFS(db)
      val filename = Paths.get(file.filename).getFileName.toString
      val contentType = file.contentType

      val metadataElems: Map[String, BSONValue] = dataParts map { f =>
        f._1 -> (if (f._2.size > 1) BSONArray(f._2.map(BSONString)) else BSONString(f._2.head))
      }

      val uploadDate = Some(System.currentTimeMillis())
      val fileToSave = BSON.writeDocument(metadataElems) match {
        case Success(metadata) => gridFS.fileToSave(Some(filename), contentType, uploadDate, metadata)
        case _ => gridFS.fileToSave(Some(filename), contentType, uploadDate)
      }

      val inputStream = new BufferedInputStream(new FileInputStream(file.ref.toFile))
      gridFS.writeFromInputStream(fileToSave, inputStream)
        .map(f => FileInfo(f.id.asInstanceOf[BSONObjectID].stringify,
          filename, contentType, uploadDate, Json.toJson(metadataElems).as[JsObject]))
    }
  }

  override def findById(id: String): Future[File] = BSONObjectID.parse(id) match {
    case Success(fileId) => database.flatMap(db => {
      val gridFS = GridFS(db)
      gridFS.find(BSONDocument("_id" -> fileId)).head.map { f =>
        val streams = GridFSStreams(gridFS)
        File(f.id.asInstanceOf[BSONObjectID].stringify,
          f.filename.get, f.contentType, f.length, f.uploadDate, streams.source(f))
      }
    })
  }

  override def findInfoById(id: String): Future[FileInfo] = BSONObjectID.parse(id) match {
    case Success(fileId) => database.flatMap(db => {
      val gridFS = GridFS(db)
      gridFS.find(BSONDocument("_id" -> fileId)).head.map { f =>
        FileInfo(f.id.asInstanceOf[BSONObjectID].stringify, f.filename.get, f.contentType, f.uploadDate,
          Json.toJson(f.metadata).as[JsObject])
      }
    })
  }


  override def findAll(projectId: String): Future[Seq[FileInfo]] = collection map {
    _.find(Json.obj("metadata.projectId" -> projectId), Option.empty[JsObject]).cursor[FileInfo]()
  } flatMap {
    _.collect[Seq](-1, Cursor.FailOnError[Seq[FileInfo]]())
  }
}

