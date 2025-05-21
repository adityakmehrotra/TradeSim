package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.AssetRequest;
import com.adityamehrotra.paper_trader.model.Asset;
import com.adityamehrotra.paper_trader.repository.AssetRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final MongoTemplate mongoTemplate;

    public AssetService(AssetRepository assetRepository, MongoTemplate mongoTemplate) {
        this.assetRepository = assetRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public int getNextID() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "assetID"));
        query.limit(1);

        Asset lastAsset = mongoTemplate.findOne(query, Asset.class);
        if (lastAsset == null) {
            return 1;
        } else {
            return lastAsset.getAssetID() + 1;
        }
    }

    @Transactional
    public void addAsset(AssetRequest asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset cannot be null");
        }

        if (asset.getTicker() == null || asset.getTicker().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }

        if (asset.getName() == null || asset.getName().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (asset.getType() == null || asset.getType().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }

        if ((assetRepository.findByTicker(asset.getTicker()) == null) && (assetRepository.findByName(asset.getName()) == null) && (assetRepository.findByIsin(asset.getIsin()) == null)) {
            int assetID = getNextID();

            Asset newAsset = new Asset(
                    assetID,
                    asset.getTicker(),
                    asset.getName(),
                    asset.getType(),
                    asset.getIsin()
            );

            assetRepository.save(newAsset);
        }
    }
}
