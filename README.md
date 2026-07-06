# TradeSim

TradeSim is a simulated stock exchange you can trade against. Every symbol runs its own live order book with bot market makers, so orders match, fill, and move prices without any external market data.

## Screenshots

_Add your own screenshots here. The trading screen and the portfolio view are the two worth showing._

<!-- ![Trading screen](docs/screenshots/trading-screen.png) -->
<!-- ![Portfolio](docs/screenshots/portfolio.png) -->

## How it works

The interesting parts are the matching engine and the accounting, so here is what happens under the hood.

### Order matching

Each symbol has a central limit order book. Incoming orders match on price first, then on arrival time within a price level, which is the same priority rule real exchanges use. The book supports limit and market orders, partial fills, and cancels. An aggressive order trades at the resting order's price, so the passive side sets the price and the taker gets any price improvement.

The engine lives in its own package with no framework or database imports. Prices are whole cents and quantities are whole shares, so matching is exact integer arithmetic and the results are easy to test. The suite checks the properties that matter: price and time priority, conservation of shares across a match, that the book is never left crossed, and that a partially filled order can still be cancelled.

Nothing external feeds the market. Bot market makers quote several levels on each side around a reference price, and noise traders send orders that print trades and nudge the price along a random walk. The result is a book with real depth and a tape that keeps moving on its own.

### Portfolio accounting

Placing a buy reserves cash and placing a sell reserves shares, so a resting order cannot be spent twice. When an order fills, the fill settles against the portfolio: a buy adds a FIFO lot and debits cash, a sell consumes lots oldest first and credits cash. Realized profit comes from the FIFO cost basis of the shares sold, and unrealized profit is marked against the last trade price. A market buy walks the book to size the order to what the account can actually afford, so cash never goes negative.

## Getting started

You need [Docker](https://www.docker.com/). From the repository root:

```sh
docker compose up --build
```

That starts MongoDB, the backend, and the frontend together. Open the app at http://localhost:5173. The API runs at http://localhost:5001, with request docs at http://localhost:5001/documentation.

There are no accounts. The first visit creates an anonymous session, stored in a cookie, with a starter portfolio of virtual cash. The Account page resets it whenever you want a clean slate.

### Running the pieces directly

If you would rather run the backend and frontend without Docker, you need Java 17, Node 20, and a MongoDB instance.

Start MongoDB, for example with `docker run -p 27017:27017 mongo:7`, then run the backend:

```sh
cd server
./mvnw spring-boot:run
```

The backend reads `MONGODB_URI` from the environment and falls back to `mongodb://localhost:27017`.
See `server/.env.example`. In another terminal, run the frontend:

```sh
cd client
cp .env.example .env
npm install
npm run dev
```

## Limitations

This is a simulator, and it makes some deliberate simplifications.

Prices are synthetic. They come from the bots and a random walk, not from any real market. Each order book lives in memory in a single process, so restarting the backend rebuilds the books from scratch; cash and positions persist in MongoDB, but resting orders do not survive a restart. Trading is long only, with no shorting, margin, or fees, and every order fills against the simulated liquidity rather than real counterparties. Sessions are tied to a browser cookie, so clearing cookies starts a new account.

## Built with

React and Vite on the frontend, Spring Boot on the backend, and MongoDB for storage.

## License

Distributed under the MIT License. See [LICENSE.txt](LICENSE.txt).
