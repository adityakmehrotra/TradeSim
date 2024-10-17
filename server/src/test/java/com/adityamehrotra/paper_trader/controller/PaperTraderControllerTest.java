package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.PaperTrader;
import com.adityamehrotra.paper_trader.repository.PaperTraderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaperTraderController.class)
public class PaperTraderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PaperTraderRepository paperTraderRepository;

  @Test
  public void testCreate() throws Exception {
    PaperTrader paperTrader = new PaperTrader();
    when(paperTraderRepository.save(paperTrader)).thenReturn(paperTrader);

    mockMvc.perform(post("/paper_trader/test/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{}"));
  }

  @Test
  public void testGetAllAuthors() throws Exception {
    when(paperTraderRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/paper_trader/test/all"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  public void testFindOneById() throws Exception {
    PaperTrader paperTrader = new PaperTrader();
    when(paperTraderRepository.findById("1")).thenReturn(Optional.of(paperTrader));

    mockMvc.perform(get("/paper_trader/test/1"))
        .andExpect(status().isOk())
        .andExpect(content().json("{}"));
  }

  @Test
  public void testUpdate() throws Exception {
    PaperTrader paperTrader = new PaperTrader();
    when(paperTraderRepository.save(paperTrader)).thenReturn(paperTrader);

    mockMvc.perform(put("/paper_trader/test/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(content().json("{}"));
  }

  @Test
  public void testDeleteById() throws Exception {
    mockMvc.perform(delete("/paper_trader/test/delete/1"))
        .andExpect(status().isOk());
  }
}