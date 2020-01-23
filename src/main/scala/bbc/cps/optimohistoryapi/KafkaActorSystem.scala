package bbc.cps.optimohistoryapi

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

object KafkaActorSystem {
  implicit val system: ActorSystem = akka.actor.ActorSystem("kafka")

  val decider: Supervision.Decider = {
    case _: Exception => Supervision.Resume
    case _ => Supervision.Stop
  }

  implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
}
