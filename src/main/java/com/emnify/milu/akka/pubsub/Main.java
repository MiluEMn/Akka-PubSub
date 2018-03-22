package com.emnify.milu.akka.pubsub;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSubSettings;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {

    Config conf = ConfigFactory.load("application.conf");
    ActorSystem system = ActorSystem.create("pubsub", conf);

    int nodeId = conf.getInt("node-id");
    String topic = conf.getString("topic");
    ActorRef mediator = system
        .actorOf(Mediator.props(DistributedPubSubSettings.create(system)), "mediator");
    system.actorOf(Publisher.props(mediator, nodeId, topic), "publisher" + nodeId);
    system.actorOf(Subscriber.props(mediator, topic), "subscriber" + nodeId);

    System.in.read();
    system.terminate();
  }
}
