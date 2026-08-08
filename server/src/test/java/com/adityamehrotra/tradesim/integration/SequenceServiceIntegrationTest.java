package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.model.Counter;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.SequenceService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/** The counter has to stay unique under load and has to respect ids that already exist. */
class SequenceServiceIntegrationTest extends IntegrationTestBase {
  private static final String SEQUENCE = "testSequence";

  @Autowired private SequenceService sequenceService;
  @Autowired private MongoTemplate mongoTemplate;

  @Test
  void handsOutEveryNumberOnlyOnceUnderConcurrency() throws Exception {
    int threads = 16;
    int perThread = 25;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    Set<Integer> seen = ConcurrentHashMap.newKeySet();

    try {
      List<Callable<Void>> work =
          IntStream.range(0, threads)
              .<Callable<Void>>mapToObj(
                  t ->
                      () -> {
                        for (int i = 0; i < perThread; i++) {
                          seen.add(sequenceService.next(SEQUENCE));
                        }
                        return null;
                      })
              .toList();
      for (Future<Void> future : pool.invokeAll(work)) {
        future.get();
      }
    } finally {
      pool.shutdownNow();
    }

    // Reading the highest id and adding one is exactly what let two callers pick the same number.
    assertEquals(threads * perThread, seen.size(), "every allocation should be unique");
    assertEquals(1, seen.stream().min(Integer::compareTo).orElseThrow());
    assertEquals(threads * perThread, seen.stream().max(Integer::compareTo).orElseThrow());
  }

  @Test
  void carriesOnFromIdsThatAlreadyExist() {
    mongoTemplate.save(new Session("existing-session", 41));
    mongoTemplate.remove(counterQuery(SequenceService.ACCOUNT_ID), Counter.class);

    sequenceService.seedFromExistingData();

    assertEquals(42, sequenceService.next(SequenceService.ACCOUNT_ID));
  }

  @Test
  void seedingAgainNeverRewindsTheCounter() {
    sequenceService.next(SequenceService.ACCOUNT_ID);
    int reached = sequenceService.next(SequenceService.ACCOUNT_ID);

    sequenceService.seedFromExistingData();
    sequenceService.seedFromExistingData();

    assertTrue(
        sequenceService.next(SequenceService.ACCOUNT_ID) > reached,
        "re-seeding must not hand back a number that was already used");
  }

  @Test
  void twoSessionsNeverShareAnAccount() {
    Set<Integer> accounts = Set.of(openSession().accountId(), openSession().accountId());

    assertEquals(2, accounts.size());
  }

  private static Query counterQuery(String name) {
    return Query.query(Criteria.where("_id").is(name));
  }
}
