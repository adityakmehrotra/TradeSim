package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        securityRepository.saveAll(securityModelList);
    }

    public Set<SecurityModel> getSuggestion(String userInput) {
        userInput = userInput.toLowerCase();
        Set<SecurityModel> codeList = new HashSet<>();

        for (SecurityModel securityModel : securityRepository.findAll()) {
            if (securityModel.getCode().toLowerCase().startsWith(userInput)) {
                codeList.add(securityModel);
            } else if (securityModel.getName().toLowerCase().startsWith(userInput)) {
                codeList.add(securityRepository.findById(securityModel.getCode()).get());
            }
        }
        return codeList;
    }
}
