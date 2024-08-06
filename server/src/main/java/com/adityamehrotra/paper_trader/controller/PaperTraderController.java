package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.PaperTrader;
import com.adityamehrotra.paper_trader.repository.PaperTraderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/test")
public class PaperTraderController {
  private final PaperTraderRepository paperTraderRepository;

  public PaperTraderController(PaperTraderRepository paperTraderRepository) {
    this.paperTraderRepository = paperTraderRepository;
  }

  @PostMapping("/create")
  public PaperTrader create(@RequestBody PaperTrader paperTrader) {
    return paperTraderRepository.save(paperTrader);
  }

  @GetMapping("/all")
  public List<PaperTrader> getAllAuthors() {
    return paperTraderRepository.findAll();
  }

  @GetMapping("/{id}")
  public Optional<PaperTrader> findOneById(@PathVariable String id) {
    return paperTraderRepository.findById(id);
  }

  @PutMapping("/update")
  public PaperTrader update(@RequestBody PaperTrader paperTrader) {
    return paperTraderRepository.save(paperTrader);
  }

  @DeleteMapping("/delete/{id}")
  public void deleteById(@PathVariable String id) {
    paperTraderRepository.deleteById(id);
  }
}
