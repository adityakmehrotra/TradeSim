package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.PaperTraderID;
import com.adityamehrotra.paper_trader.repository.IDRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/id")
public class PaperTraderIDController {
  private final IDRepository idRepository;

  public PaperTraderIDController(IDRepository idRepository) {
    this.idRepository = idRepository;
  }

  @GetMapping("/getID")
  public Integer create() {
    return idRepository.findAll().get(idRepository.findAll().size() - 1).getId();
  }

  @GetMapping("/all")
  public List<PaperTraderID> getAll() {
    return idRepository.findAll();
  }

  @GetMapping("/put/{id}")
  public PaperTraderID putID(@PathVariable Integer id) {
    PaperTraderID paperTraderID = new PaperTraderID();
    paperTraderID.setId(id);
    return idRepository.save(paperTraderID);
  }

  @GetMapping("/test")
  public Integer getTest() {
    return 1;
  }

  @GetMapping("/{id}")
  public Boolean findOneById(@PathVariable Integer id) {
    if (idRepository.findAll().isEmpty()) {
      return false;
    }
    return idRepository.findAll().contains(id);
  }

  @DeleteMapping("/delete/{id}")
  public void deleteById(@PathVariable String id) {
    idRepository.deleteById(id);
  }
}
