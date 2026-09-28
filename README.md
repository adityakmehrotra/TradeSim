# TradeSim

TradeSim is a simulated stock exchange you can trade against. Every symbol runs its own live order book with bot market makers, so orders match, fill, and move prices without any external market data.

## How it works

The interesting parts are the matching engine and the accounting, so here is what happens under the hood.

### Order matching

Each symbol has a central limit order book. Incoming orders match on price first, then on arrival time within a price level, which is the same priority rule real exchanges use. The book supports limit and market orders, partial fills, and cancels. An aggressive order trades at the resting order's price, so the passive side sets the price and the taker gets any price improvement.

The engine lives in its own package with no framework or database imports. Prices are whole cents and quantities are whole shares, so matching is exact integer arithmetic and the results are easy to test. The suite checks the properties that matter: price and time priority, conservation of shares across a match, that the book is never left crossed, and that a partially filled order can still be cancelled.

Nothing external feeds the market. Bot market makers quote several levels on each side around a reference price, and noise traders send orders that print trades and nudge the price along a random walk. The result is a book with real depth and a tape that keeps moving on its own.

### Portfolio accounting

Placing a buy reserves cash and placing a sell reserves shares, so a resting order cannot be spent twice. When an order fills, the fill settles against the portfolio: a buy adds a FIFO lot and debits cash, a sell consumes lots oldest first and credits cash. Realized profit comes from the FIFO cost basis of the shares sold, and unrealized profit is marked against the last trade price. A market buy walks the book to size the order to what the account can actually afford, so cash never goes negative.

Balances are stored as whole cents, not dollars. A dollar amount held as a floating point number cannot represent every cent exactly, so a long run of fills leaves the balance off by fractions of a cent and the books stop adding up. Cash, reserved cash, cost basis, and realized profit are all integers end to end, and the tests check that a few hundred fills at awkward prices leave the balance exact. Amounts are divided by a hundred only when they are displayed.

## Getting started

You need [Docker](https://www.docker.com/). From the repository root:

```sh
docker compose up --build
```

That starts MongoDB, the backend, and the frontend together. Open the app at http://localhost:5173. The API runs at http://localhost:5001, with request docs at http://localhost:5001/documentation.

There are no accounts. The first visit creates an anonymous session, stored in a cookie, with a starter portfolio of virtual cash. The Account page resets it whenever you want a clean slate.

### Running the pieces directly

If you would rather run the backend and frontend without Docker, you need Java 17, Node 22, and a MongoDB instance.

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

## Deploying

The frontend is a static build on Vercel at `https://tradesim.adityamehrotra.com`. The backend runs on Railway at `https://api.tradesim.adityamehrotra.com`. Both are under one registrable domain, so the session cookie is same site and does not have to be a cross site cookie. The database is a MongoDB Atlas cluster.

The backend service builds from `server/Dockerfile`, with `server` as the service root because that is where `railway.json` lives. That file points the health check at the readiness probe, so a new deployment only takes traffic once the startup sweep and any required migration have finished. Keep it at one replica: the order books live in memory, and a second instance refuses to start rather than trade against a book it cannot see.

Add `api.tradesim.adityamehrotra.com` as a custom domain on the service and create the CNAME and TXT records Railway shows for it. Railway does not publish a fixed egress address, so Atlas has to accept connections from anywhere and the database user needs a strong password of its own.

Set these on the backend service:

| Variable | Value |
| --- | --- |
| `MONGODB_URI` | the Atlas connection string, starting `mongodb+srv://`. The application names its own database. |
| `CLIENT_ORIGIN` | `https://tradesim.adityamehrotra.com`, plus any other exact origin, comma separated |
| `COOKIE_SECURE` | `true` |
| `COOKIE_SAME_SITE` | `Lax` |
| `TRADESIM_INSTANCE_LEASE_WAIT_MS` | `45000` |

The lease wait matters. Deploying starts the replacement before stopping what is running, so for a moment both exist. The new instance waits for the old lease to expire instead of refusing to start, which is what lets a deployment go through without ever running two markets at once.

`PORT` is set by the host and the application reads it. Forwarded headers are trusted because nothing can reach the container except the platform edge, and the rate limits key on the caller's address, which would otherwise be the proxy for everyone.

On Vercel, set `VITE_API_BASE_URL` to `https://api.tradesim.adityamehrotra.com` for production. Vite reads it at build time, so a change takes effect on the next deployment rather than the running one. Preview deployments live on `vercel.app` origins, which are cross site to the API and cannot hold the session cookie, so only production is wired up. The frontend sends its session cookie with every request, and the backend only answers origins named in `CLIENT_ORIGIN`. `client/vercel.json` rewrites every path to `index.html`, so a page can be refreshed or linked to directly.

To roll back, redeploy the previous deployment from the Railway dashboard. Balances are written as both cents and dollars during the current compatibility window, so an older build reads current balances rather than stale ones.

## Limitations

This is a simulator, and it makes some deliberate simplifications.

Prices are synthetic. They come from the bots and a random walk, not from any real market. Each order book lives in memory in a single process, so restarting the backend rebuilds the books from scratch; cash and positions persist in MongoDB, but resting orders do not survive a restart. Trading is long only, with no shorting, margin, or fees, and every order fills against the simulated liquidity rather than real counterparties. Sessions are tied to a browser cookie, so clearing cookies starts a new account.

Only one backend instance can run at a time. Since the books are in memory, a second instance would match orders against a book the first cannot see. It would also run the startup sweep, which releases every reservation in the database on the assumption that no order book survived the restart, and that would free the cash and shares behind the first instance's live orders. Instances take a lease in MongoDB at startup, and one that finds the lease held refuses to start. Until that sweep and the required migrations finish, the backend reports itself not ready at `/actuator/health/readiness` and refuses new orders.

## Built with

React and Vite on the frontend, Spring Boot on the backend, and MongoDB for storage.

## License

Distributed under the MIT License. See [LICENSE.txt](LICENSE.txt).
