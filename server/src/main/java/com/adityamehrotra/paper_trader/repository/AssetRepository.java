package com.adityamehrotra.paper_trader.repository;

import com.adityamehrotra.paper_trader.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends MongoRepository<Asset, Integer> {
    Asset findByTicker(String ticker);
    Asset findByName(String name);
    Asset findByIsin(String isin);
}
