package bbc.cps.assetstoreaggregate.model

import org.json4s.JsonAST.JValue

case class AssetDocument(assetId: String, workingBranch: JValue, publishedBranch: Option[JValue])
