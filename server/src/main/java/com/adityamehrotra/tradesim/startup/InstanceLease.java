package com.adityamehrotra.tradesim.startup;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Lets one instance at a time own the market. Order books are held in memory, so a second instance
 * would match orders against a book the first cannot see, and its startup sweep would release the
 * reservations behind the first instance's resting orders.
 *
 * <p>The lease is one document with an owner and an expiry. Taking it is an upsert that only
 * matches an expired lease or this instance's own, so a live lease held elsewhere collides on the
 * document id and the start fails there and then. The expiry is what lets an instance that was
 * killed be replaced without anyone clearing the document by hand.
 */
@Component
public class InstanceLease {
  static final String COLLECTION = "TradeSim-Lease";
  static final String LEASE_ID = "market";

  private static final Logger log = LoggerFactory.getLogger(InstanceLease.class);

  private final MongoTemplate mongoTemplate;
  private final boolean enabled;
  private final long holdMillis;
  private final String owner = UUID.randomUUID().toString();

  private volatile boolean held;

  public InstanceLease(
      MongoTemplate mongoTemplate,
      @Value("${tradesim.instance.lease-enabled:true}") boolean enabled,
      @Value("${tradesim.instance.lease-hold-ms:30000}") long holdMillis) {
    this.mongoTemplate = mongoTemplate;
    this.enabled = enabled;
    this.holdMillis = holdMillis;
  }

  @PostConstruct
  public void acquire() {
    if (!enabled) {
      // Nothing is holding the market, so there is nothing for readiness to wait on either.
      held = true;
      return;
    }
    long now = System.currentTimeMillis();
    try {
      mongoTemplate.upsert(
          Query.query(
              Criteria.where("_id")
                  .is(LEASE_ID)
                  .orOperator(
                      Criteria.where("expiresAt").lte(now), Criteria.where("owner").is(owner))),
          new Update().set("owner", owner).set("expiresAt", now + holdMillis),
          COLLECTION);
    } catch (DuplicateKeyException e) {
      log.error(
          "Another TradeSim instance holds the market lease. Order books live in memory in one "
              + "process, so this instance will not start. Stop the other one or wait for its "
              + "lease to expire.");
      throw new IllegalStateException("The market lease is held by another instance");
    }
    held = true;
  }

  public boolean isHeld() {
    return held;
  }

  /**
   * Keeps the expiry ahead of now while this instance runs. A renewal that matches nothing means
   * another instance took the lease over, and this one is no longer the market anyone is trading
   * against.
   */
  @Scheduled(fixedDelayString = "${tradesim.instance.heartbeat-ms:10000}")
  public void renew() {
    if (!enabled || !held) {
      return;
    }
    long matched =
        mongoTemplate
            .updateFirst(
                Query.query(Criteria.where("_id").is(LEASE_ID).and("owner").is(owner)),
                new Update().set("expiresAt", System.currentTimeMillis() + holdMillis),
                COLLECTION)
            .getMatchedCount();
    if (matched == 0) {
      held = false;
      log.error("Lost the market lease to another instance, so this one is no longer ready");
    }
  }
}
