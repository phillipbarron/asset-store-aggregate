package bbc.cps.assetstoreaggregate.model

object EventType extends Enumeration {
  type EventType = Value

  val PUBLISHED = Value("PUBLISHED")
  val SAVED = Value("SAVED")
  val CREATED = Value("CREATED")
  val DELETED = Value("DELETED")
  val RESTORED = Value("RESTORED")
  val PUBLISHED_MINOR_CHANGE = Value("PUBLISHED_MINOR_CHANGE")
  val PUBLISHED_NEWS_UPDATE = Value("PUBLISHED_NEWS_UPDATE")
  val WITHDRAWN = Value("WITHDRAWN")
}
