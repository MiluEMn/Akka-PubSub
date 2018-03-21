package com.emnify.milu.akka.pubsub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator.Publish;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class Publisher extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), getSelf());

  public static Props props(int nodeId, String topic) {

    return Props.create(Publisher.class, nodeId, topic);
  }

  public Publisher(int nodeId, String topic) {

    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    getContext().system().scheduler().schedule(
        Duration.Zero(),
        Duration.create(2L, TimeUnit.SECONDS),
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
