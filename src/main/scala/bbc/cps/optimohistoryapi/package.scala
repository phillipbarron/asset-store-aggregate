package bbc.cps.assetstoreaggregate

package object exceptions {
  case class BadRequestException(message: String) extends Exception(message)
  case class DatabaseException(message: String) extends Exception(message)
  case class AssetNotFoundException(message: String) extends Exception(message)
}
