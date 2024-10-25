package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityService {
    private final SecurityRepository securityRepository;

    public SecurityService(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    public SecurityModel createOne(SecurityModel securityModel) {
        return securityRepository.save(securityModel);
    }

    public void createMany(List<SecurityModel> securityModelList) {
        for (SecurityModel securityModel : securityModelList) {
            securityRepository.save(securityModel);
        }
    }
}
