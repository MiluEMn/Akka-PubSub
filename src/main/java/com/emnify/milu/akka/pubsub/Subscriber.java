package com.emnify.milu.akka.pubsub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import akka.cluster.pubsub.DistributedPubSubMediator.Unsubscribe;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Subscriber extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private Map<ActorRef, Long> peers;
  private String topic = "";

  public Subscriber(String topic) {

    this.topic = topic;
    peers = new HashMap<>();
  }

  @Override
  public void preStart() {

    log.info("PreStart");

    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    mediator.tell(new Subscribe(topic, getSelf()), getSelf());
  }

  @Override
  public void postStop() {

    log.info("PostStop");

    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    mediator.tell(new Unsubscribe(topic, getSelf()), getSelf());

    peers.forEach((actorRef, aLong) -> log.info("First heard from {} at {}",
        actorRef, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(aLong)));

    log.info("Done with postStop");
  }

  public static Props props(String topic) {

    return Props.create(Subscriber.class, topic);
  }

  @Override
  public Receive createReceive() {

    return receiveBuilder()
        .match(SubscribeAck.class, msg -> log.info("Successfully subscribed"))
        .matchAny(msg -> {
          if (!peers.containsKey(getSender())) {
            peers.put(getSender(), System.currentTimeMillis());
          }
          log.info("Got: {}", msg);
        })
        .build();
  }
}
