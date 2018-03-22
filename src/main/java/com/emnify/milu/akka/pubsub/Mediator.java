package com.emnify.milu.akka.pubsub;

import akka.actor.Address;
import akka.actor.Deploy;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.pubsub.DistributedPubSubSettings;
import scala.Function1;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class Mediator extends DistributedPubSubMediator {

  public static Props props(DistributedPubSubSettings settings) {

    return Props.create(Mediator.class, settings).withDeploy(Deploy.local());
  }

  public Mediator(DistributedPubSubSettings settings) {

    super(settings);
  }

  @Override
  public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object message) {

    log().info("Mediator got: {}", message);
    super.aroundReceive(receive, message);
  }

  @Override
  public void gossip() {

    nodes().foreach((Function1<Address, Void>) node -> {
      if (!node.equals(self().path().address())) {
        gossipTo(node);
      }
      return null;
    });
  }
}
