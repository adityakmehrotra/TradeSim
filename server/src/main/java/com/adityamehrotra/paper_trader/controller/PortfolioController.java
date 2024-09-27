package com.adityamehrotra.paper_trader.controller;

import com.adityamehrotra.paper_trader.model.Asset;
import com.adityamehrotra.paper_trader.model.Portfolio;
import com.adityamehrotra.paper_trader.model.SecurityModel;
import com.adityamehrotra.paper_trader.model.Transaction;
import com.adityamehrotra.paper_trader.repository.AccountRepository;
import com.adityamehrotra.paper_trader.repository.PortfolioRepository;
import com.adityamehrotra.paper_trader.repository.SecurityRepository;
import com.adityamehrotra.paper_trader.repository.TransactionRepository;
import com.adityamehrotra.paper_trader.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/paper_trader/portfolio")
public class PortfolioController {
  private final AccountRepository accountRepository;
  private final PortfolioRepository portfolioRepository;
  private final SecurityRepository securityRepository;
  private final TransactionRepository transactionRepository;
  private final PortfolioService portfolioService;

  public PortfolioController(
          PortfolioRepository portfolioRepository,
          SecurityRepository securityRepository,
          TransactionRepository transactionRepository,
          AccountRepository accountRepository,
          PortfolioService portfolioService) {
    this.portfolioRepository = portfolioRepository;
    this.securityRepository = securityRepository;
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.portfolioService = portfolioService;
  }

  @Tag(name = "Post Portfolio", description = "POST method of Portfolio APIs")
  @Operation(
      summary = "Add Portfolio",
      description =
          "Add an Portfolio. The response is a Portfolio object with the parameters given.")
  @PostMapping("/create")
  public Portfolio create(
      @Parameter(description = "Portfolio object to be created", required = true) @RequestBody
      Portfolio portfolio) {
    return portfolioService.addPortfolio(portfolio);
  }

  @Tag(name = "Delete Portfolio", description = "DELETE methods of Portfolio APIs")
  @Operation(
      summary = "Delete Portfolio",
      description = "Delete a Portfolio.")
  @DeleteMapping("/remove")
  public void delete(@RequestParam Integer id) {
    List<Integer> currPortfolioList = new ArrayList<>();
    for (int i = 0;
         i
                 < accountRepository
                 .findById(portfolioRepository.findById(id).get().getAccountID())
                 .get()
                 .getPortfolioList()
                 .size();
         i++) {
      Integer curr =
              accountRepository
                      .findById(portfolioRepository.findById(id).get().getAccountID())
                      .get()
                      .getPortfolioList()
                      .get(0);
      if (id != curr) {
        currPortfolioList.add(curr);
      }
    }
    accountRepository
            .findById(portfolioRepository.findById(id).get().getAccountID())
            .get()
            .setPortfolioList(currPortfolioList);
    portfolioRepository.deleteById(id);
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Specific Portfolio by Portfolio ID",
      description =
          "Get the information for a specific portfolio. The response is the Portfolio object that matches the id.")
  @GetMapping("/get")
  public Portfolio getAllInfo(@RequestParam Integer id) {
    return portfolioRepository.findById(id).get();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Account ID by Portfolio ID",
      description =
          "Get the Account ID for a specific portfolio. The response is an integer of the Account ID.")
  @GetMapping("/get/accountID")
  public Integer getAccountID(@RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getAccountID();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Name by Portfolio ID",
      description =
          "Get the Name for a specific portfolio. The response is an String of the Name.")
  @GetMapping("/get/name")
  public String getPortfolioName(@RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getPortfolioName();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Cash by Portfolio ID",
      description =
          "Get the Cash for a specific portfolio. The response is a Double of the Cash value.")
  @GetMapping("/get/cash")
  public Double getCash(@Parameter(description = "Portfolio ID whose cash needs to be retrieved", required = true)
  @RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getCashAmount();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Initial Cash by Portfolio ID",
      description =
          "Get the Initial Cash for a specific portfolio. The response is a Double of the Initial Cash value.")
  @GetMapping("/get/initcash")
  public Double getInitCash(@Parameter(description = "Portfolio ID whose initial cash needs to be retrieved", required = true)
  @RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getInitialBalance();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Transactions by Portfolio ID",
      description =
          "Get the Transactions for a specific portfolio. The response is a List of Integers, each integer is a transactionID.")
  @GetMapping("/get/transactions")
  public List<Integer> getTransactionList(@Parameter(description = "Portfolio ID whose transaction list needs to be retrieved", required = true)
  @RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getTransactionList();
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Assets by Portfolio ID",
      description =
          "Get the Assets for a specific portfolio. The response is a List of Assets.")
  @GetMapping("/get/assets")
  public List<Asset> getAssets(@Parameter(description = "Portfolio ID whose initial cash needs to be retrieved", required = true)
  @RequestParam Integer id) {
    List<Asset> assetsList =
            new ArrayList<>(portfolioRepository.findById(id).get().getAssets().values());
    return assetsList;
  }

  @Tag(name = "Get Portfolio", description = "GET methods of Portfolio APIs")
  @Operation(
      summary = "Get Map of Asset Ticker Symbol and Asset by Portfolio ID",
      description =
          "Get the Map of Asset Ticker Symbol and Asset for a specific portfolio. The response is a Map of Asset Ticker Symbol and Asset.")
  @GetMapping("/get/assetsMap")
  public Map<String, Asset> getAssetsMap(@Parameter(description = "Portfolio ID whose assets map needs to be retrieved", required = true)
  @RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getAssets();
  }

  @GetMapping("/get/assetsMap/shares")
  public Double getAssetsMap(@RequestParam Integer id, @RequestParam String code) {
    return portfolioRepository.findById(id).get().getAssets().get(code).getSharesOwned();
  }

  @GetMapping("/get/holdings")
  public Set<SecurityModel> getHolding(@RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getHoldings();
  }

  @GetMapping("/get/assets/avgValue")
  public Map<String, Double> getAssetsAvgValue(@RequestParam Integer id) {
    return portfolioRepository.findById(id).get().getAssetsAvgValue();
  }

  @PostMapping("/add/holding")
  public Portfolio addHolding(@RequestParam Integer id, @RequestParam String code) {
    if (code.equals("Cash")) {
      Set<SecurityModel> holdings = new HashSet<>();
      SecurityModel cashSecurityModel = new SecurityModel("Cash", null, null, null, null, null, null);
      holdings.add(cashSecurityModel);
      Portfolio portfolio = new Portfolio(id,
              getAccountID(id),
              getPortfolioName(id),
              getCash(id),
              getInitCash(id),
              getTransactionList(id),
              getAssetsMap(id),
              holdings,
              getAssetsAvgValue(id));
      return portfolioRepository.save(portfolio);
    }
    Set<SecurityModel> holdingsSet = portfolioRepository.findById(id).get().getHoldings();
    holdingsSet.add(securityRepository.findById(code).get());
    Portfolio portfolio =
            new Portfolio(
                    id,
                    getAccountID(id),
                    getPortfolioName(id),
                    getCash(id),
                    getInitCash(id),
                    getTransactionList(id),
                    getAssetsMap(id),
                    holdingsSet,
                    getAssetsAvgValue(id));
    return portfolioRepository.save(portfolio);
  }

  public Portfolio editAsset(@RequestParam Integer id, @RequestParam String code, Double shareAmt, Double cashAmt) {
    Map<String, Asset> assets = portfolioRepository.findById(id).get().getAssets();
    Map<String, Double> assetsAvgValue = portfolioRepository.findById(id).get().getAssetsAvgValue();
    if (!assets.isEmpty()) {
      if (assets.containsKey(code)) {
        Double currAmt = assets.get(code).getSharesOwned();
        if (currAmt - shareAmt == 0) {
          assets.remove(code);
          assetsAvgValue.remove(code);
          Set<SecurityModel> holdings = portfolioRepository.findById(id).get().getHoldings();
          holdings.remove(code);
          Portfolio portfolio = new Portfolio(
                  id,
                  getAccountID(id),
                  getPortfolioName(id),
                  getCash(id),
                  getInitCash(id),
                  getTransactionList(id),
                  assets,
                  holdings,
                  assetsAvgValue);
          return portfolioRepository.save(portfolio);
        } else {
          Asset asset = assets.get(code);
          asset.setSharesOwned(currAmt - shareAmt);
          asset.setInitialCashInvestment(assets.get(code).getInitialCashInvestment() - cashAmt);
          assets.put(code, asset);

          Portfolio portfolio = new Portfolio(
                  id,
                  getAccountID(id),
                  getPortfolioName(id),
                  getCash(id),
                  getInitCash(id),
                  getTransactionList(id),
                  assets,
                  getHolding(id),
                  getAssetsAvgValue(id));
          return portfolioRepository.save(portfolio);
        }
      }
    }
    return null;
  }

  @PostMapping("/add/asset")
  public Portfolio addAsset(
          @RequestParam Integer id, @RequestParam String code, @RequestBody Asset asset, @RequestParam Integer transactionID) {
    if (code.equals("Cash")) {
      Map<String, Asset> assets = new HashMap<>();
      assets.put(code, asset);
      Map<String, Double> assetsAvgValue = new HashMap<>();
      assetsAvgValue.put("Cash", asset.getInitialCashInvestment());
      Portfolio portfolio = new Portfolio(id,
              getAccountID(id),
              getPortfolioName(id),
              getCash(id),
              getInitCash(id),
              getTransactionList(id),
              assets,
              getHolding(id),
              assetsAvgValue);
      return portfolioRepository.save(portfolio);
    }
    code = code.toUpperCase();
    Map<String, Asset> assets;
    Map<String, Asset> assetsMap = portfolioRepository.findById(id).get().getAssets();
    Map<String, Double> assetsAvgValue = portfolioRepository.findById(id).get().getAssetsAvgValue();
    System.out.println(assetsAvgValue);

    if (assetsMap == null) {
      assets = new HashMap<>();
    } else {
      assets = assetsMap;
    }
    if (!assets.isEmpty()) {
      if (assets.containsKey(code)) {
        Double currAmt = assets.get(code).getSharesOwned();
        asset.setSharesOwned(currAmt + asset.getSharesOwned());
        Double tempAmt = assetsAvgValue.get(code) * currAmt;
        System.out.println("Temp Amt: " + tempAmt);
        System.out.println("Asset: " + asset);
        Double newAmt = transactionRepository.findById(transactionID).get().getShareAmount() * transactionRepository.findById(transactionID).get().getCurrPrice();
        System.out.println("New Amt: " + newAmt);
        assetsAvgValue.put(code, (tempAmt + newAmt) / asset.getSharesOwned());
      } else {
        assetsAvgValue.put(code, asset.getInitPrice());
      }
    }
    assets.put(code, asset);
    Portfolio portfolio =
            new Portfolio(
                    id,
                    getAccountID(id),
                    getPortfolioName(id),
                    getCash(id),
                    getInitCash(id),
                    getTransactionList(id),
                    assets,
                    addHolding(id, code).getHoldings(),
                    assetsAvgValue);
    return portfolioRepository.save(portfolio);
  }

  @PostMapping("/add/transaction")
  public Portfolio addTransaction(@RequestParam Integer id, @RequestParam Integer transactionID) {
    List<Integer> transactions;
    List<Integer> transactionList = portfolioRepository.findById(id).get().getTransactionList();
    if (transactionList == null) {
      transactions = new ArrayList<>();
    } else {
      transactions = transactionList;
    }
    transactions.add(transactionID);
    Portfolio portfolio;
    Transaction transaction = transactionRepository.findById(transactionID).get();
    if (transaction.getOrderType().equals("Buy")) {
      Asset asset = new Asset(transaction.getShareAmount(), transaction.getCashAmount(), transaction.getGmtTime(), transaction.getCurrPrice());
      addAsset(id, transactionRepository.findById(transactionID).get().getSecurityCode(), asset, transactionID);
      portfolio =
              new Portfolio(
                      id,
                      getAccountID(id),
                      getPortfolioName(id),
                      getCash(id) - transaction.getCashAmount(),
                      getInitCash(id),
                      transactions,
                      getAssetsMap(id),
                      addHolding(id, "Cash").getHoldings(),
                      getAssetsAvgValue(id));
    } else if (transaction.getOrderType().equals("Fund")) {
      Asset asset = new Asset(transaction.getShareAmount(), transaction.getCashAmount(), transaction.getGmtTime(), transaction.getCurrPrice());
      addAsset(id, transactionRepository.findById(transactionID).get().getSecurityCode(), asset, transactionID);
      portfolio =
              new Portfolio(
                      id,
                      getAccountID(id),
                      getPortfolioName(id),
                      getCash(id),
                      getInitCash(id),
                      transactions,
                      getAssetsMap(id),
                      addHolding(id, transactionRepository.findById(transactionID).get().getSecurityCode())
                              .getHoldings(),
                      getAssetsAvgValue(id));
    } else {
      portfolioRepository.findById(id).get().setCashAmount(getCash(id) + transactionID);
      System.out.println(portfolioRepository.findById(id).get().getAssets());
      editAsset(id, transactionRepository.findById(transactionID).get().getSecurityCode(), transactionRepository.findById(transactionID).get().getShareAmount(), transactionRepository.findById(transactionID).get().getCashAmount());
      System.out.println(portfolioRepository.findById(id).get().getAssets());
      portfolio =
              new Portfolio(
                      id,
                      getAccountID(id),
                      getPortfolioName(id),
                      getCash(id) + transaction.getCashAmount(),
                      getInitCash(id),
                      transactions,
                      getAssetsMap(id),
                      addHolding(id, transactionRepository.findById(transactionID).get().getSecurityCode())
                              .getHoldings(),
                      getAssetsAvgValue(id));
    }

    return portfolioRepository.save(portfolio);
  }

  @GetMapping("/all")
  public List<Portfolio> getAllPortfolios() {
    return portfolioRepository.findAll();
  }

  @GetMapping("/get/nextportfolioID")
  public Integer getNextPortfolioID() {
    return portfolioRepository.findAll().stream()
            .max(Comparator.comparingInt(Portfolio::getPortfolioID))
            .map(account -> account.getPortfolioID() + 1)
            .orElse(1);
  }
}
