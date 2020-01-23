package bbc.cps.optimohistoryapi.util

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AbstractAmazonS3
import com.amazonaws.services.s3.model._

import scala.collection.mutable

object AmazonS3ClientDummy extends AbstractAmazonS3 {
  
  val objects: mutable.HashMap[String, String] = mutable.HashMap.empty[String,String]

  override def getObjectAsString(bucketName: String, key: String): String = {
    val ex = new AmazonServiceException("AWS Exception")
    objects.get(key) match {
      case Some(s) => s
      case None if key.contains("notexist") => 
        ex.setStatusCode(404)
        ex.setErrorCode("NoSuchKey")
        throw ex
      case None if key.contains("forbidden") =>
        ex.setStatusCode(403)
        ex.setErrorCode("AccessDenied")
        throw ex
      case _ =>
        ex.setStatusCode(500)
        throw ex
    }
  }

  override def putObject(bucketName: String, key: String, content: String): PutObjectResult = {
    objects.put(key, content)
    new PutObjectResult()
  }
}
