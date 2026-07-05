package com.adityamehrotra.tradesim.market;

import java.util.List;

/** The fixed set of symbols the simulated exchange lists. Prices are in cents. */
public final class Instruments {
  private Instruments() {}

  public static final List<Instrument> ALL =
      List.of(
          new Instrument("NOVA", "Nova Robotics", 4250, 0, 22),
          new Instrument("ATOM", "Atom Dynamics", 11800, 2, 18),
          new Instrument("HELX", "Helix Bio", 2675, 0, 35),
          new Instrument("ORBT", "Orbit Aerospace", 8940, 1, 20),
          new Instrument("FLUX", "Fluxwave Energy", 1530, 0, 30),
          new Instrument("QBIT", "Quantum Systems", 30720, 3, 25),
          new Instrument("VERD", "Verdant Foods", 640, 0, 15),
          new Instrument("ZEPH", "Zephyr Motors", 21050, 1, 28));
}
