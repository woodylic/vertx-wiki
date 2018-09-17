package io.vertx.guides.wiki;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.guides.wiki.database.WikiDatabaseVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    // deploying verticle is an async operation so we need a future.
    // once a verticle is deployed it gets an identifier,
    // we use a String to capture it.
    Future<String> dbVerticleDeployment = Future.future();

    // One option is to create a verticle instance with new,
    // and pass the object reference to the deploy method.
    // The completer return value is a handler that simply
    // completes its future.
    vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment.completer());

    // Sequential composition with compose allows to
    // run one asynchronous operation after the other.
    // When the initial future completes successfully,
    // the composition function is invoked.
    dbVerticleDeployment.compose(id -> {

      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(
        // A class name as a string is also an option
        // to specify a verticle to deploy.
        "io.vertx.guides.wiki.http.HttpServerVerticle",
        // The DeploymentOption class allows to specify
        // a number of parameters and especially the number
        // of instances to deploy.
        new DeploymentOptions().setInstances(2),
        httpVerticleDeployment.completer());

        // The composition function returns the next future.
        // Its completion will trigger the completion of
        // the composite operation.
        return httpVerticleDeployment;

    }).setHandler(ar -> {
      // define a handler that eventually completes the MainVerticle start future.
      if(ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }
}
