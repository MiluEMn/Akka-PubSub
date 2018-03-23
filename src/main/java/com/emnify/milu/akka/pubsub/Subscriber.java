package com.emnify.milu.akka.pubsub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import akka.cluster.pubsub.DistributedPubSubMediator.Unsubscribe;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;

public class Subscriber extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private ActorRef mediator;
  private Map<ActorRef, Long> peers;
  private long currentTick = 0L;
  private String topic = "";

  public Subscriber(ActorRef mediator, String topic) {

    this.mediator = mediator;
    this.topic = topic;
    peers = new HashMap<>();
  }

  @Override
  public void preStart() {

    mediator.tell(new Subscribe(topic, getSelf()), getSelf());
  }

  @Override
  public void postStop() {

    mediator.tell(new Unsubscribe(topic, getSelf()), getSelf());
    peers.keySet().stream()
        .sorted()
        .forEach(key -> log.info(key.path() + " -> " + peers.get(key)));
  }

  public static Props props(ActorRef mediator, String topic) {

    return Props.create(Subscriber.class, mediator, topic);
  }

  @Override
  public Receive createReceive() {

    return receiveBuilder()
        .match(SubscribeAck.class, msg -> log.info("Successfully subscribed"))
        .matchAny(msg -> {
          if(msg.toString().equals("GossipTick")) {
            currentTick++;
          } else {
            if (!peers.containsKey(getSender())) {
              peers.put(getSender(), currentTick);
            }
            log.info("Got: {}", msg);
          }
        })
        .build();
  }
}
