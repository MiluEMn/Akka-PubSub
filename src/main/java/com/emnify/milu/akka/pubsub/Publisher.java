package com.emnify.milu.akka.pubsub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator.Publish;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class Publisher extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), getSelf());

  public static Props props(ActorRef mediator, int nodeId, String topic) {

    return Props.create(Publisher.class, mediator, nodeId, topic);
  }

  public Publisher(ActorRef mediator, int nodeId, String topic) {

    getContext().system().scheduler().schedule(
        Duration.Zero(),
        Duration.create(1L, TimeUnit.SECONDS),
        () -> mediator.tell(new Publish(topic, "Publisher #" + nodeId + ": Hi, guys!"), getSelf()),
        getContext().system().dispatcher()
    );
  }

  @Override
  public Receive createReceive() {

    return receiveBuilder()
        .matchAny(event -> log.info("Unexpectedly got: {}", event))
        .build();
  }
}
