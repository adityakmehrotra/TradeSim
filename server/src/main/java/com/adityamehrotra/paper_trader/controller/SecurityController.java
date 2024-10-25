package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.adityamehrotra.paper_trader.service.SecurityService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/security")
public class SecurityController {
  private final SecurityRepository securityRepository;
  private final SecurityService securityService;

  public SecurityController(SecurityRepository securityRepository, SecurityService securityService) {
    this.securityRepository = securityRepository;
    this.securityService = securityService;
  }

  @PostMapping("/create/one")
  public SecurityModel createOne(@RequestBody SecurityModel securityModel) {
    return securityService.createOne(securityModel);
  }

  @PostMapping("/create/many")
  public void createMany(@RequestBody List<SecurityModel> securityModelList) {
    securityService.createMany(securityModelList);
  }

  @GetMapping("/suggestion/{userInput}")
  public Set<SecurityModel> getSuggestion(@PathVariable String userInput) {
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

  @GetMapping("/suggestion/test/{userInput}")
  public Set<SecurityModel> getSuggestionTest(@PathVariable String userInput) {
    userInput = userInput.toLowerCase();
    return securityRepository.findSecuritiesStartingWith(userInput);
  }

  @GetMapping("/all")
  public List<SecurityModel> getAllSecurities() {
    return securityRepository.findAll();
  }

  @DeleteMapping("/delete/{id}")
  public void deleteById(@PathVariable String id) {
    securityRepository.deleteById(id);
  }
}
