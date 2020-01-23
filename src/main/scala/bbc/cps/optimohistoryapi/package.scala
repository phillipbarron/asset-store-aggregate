package bbc.cps.assetstoreaggregate

package object exceptions {
  case class BadRequestException(message: String) extends Exception(message)
  case class DatabaseException(message: String) extends Exception(message)
  case class HistoryNotFoundException(message: String) extends Exception(message)
  case class SnapshotNotFoundException(message: String) extends Exception(message)
  case class SnapshotRetrievalFailureException(message: String, statusCode: Option[Int] = None) extends Exception(message)
}
