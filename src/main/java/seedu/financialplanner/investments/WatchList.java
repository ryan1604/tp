package seedu.financialplanner.investments;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import seedu.financialplanner.exceptions.FinancialPlannerException;
import seedu.financialplanner.storage.LoadData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WatchList {
    private static WatchList watchlist = null;
    private static Logger logger = Logger.getLogger("Financial Planner Logger");
    private HashMap<String, Stock> stocks = null;
    private final String API_ENDPOINT = "https://financialmodelingprep.com/api/v3/quote/";
    private final String API_KEY = "iFumtYryBCbHpS3sDqLdVKi2SdP63vSV";

    private WatchList() {
        stocks = LoadData.loadWatchList();
        if (!stocks.isEmpty()) {
            return;
        }
        System.out.println("Initializing New watchlist.. adding AAPL and GOOGL for your reference");
        try {
            Stock apple = new Stock("AAPL");
            assert apple.getSymbol() != null && apple.getStockName() != null;
            stocks.put(apple.getSymbol(), apple);

            Stock google = new Stock("GOOGL");
            assert google.getSymbol() != null && google.getStockName() != null;
            stocks.put(google.getSymbol(), google);

        } catch (FinancialPlannerException e) {
            System.out.println(e.getMessage());
        }
    }

    public static WatchList getInstance() {
        if (watchlist == null) {
            watchlist = new WatchList();
        }
        return watchlist;
    }

    public void getLatestWatchlistInfo() throws FinancialPlannerException {
        JSONArray JSONstocks = fetchFMPStockPrices();
        extractWatchlistInfoFromJSONArray(JSONstocks);
    }

    public JSONArray fetchFMPStockPrices() throws FinancialPlannerException {
        if (stocks.isEmpty()) {
            throw new FinancialPlannerException("Empty Watchlist. Nothing to display...");
        }

        HttpClient client = HttpClient.newHttpClient();
        StringBuilder queryStocks = new StringBuilder();
        assert !stocks.isEmpty();
        for (Map.Entry<String, Stock> set : stocks.entrySet()) {
            queryStocks.append(set.getKey());
        }
        String requestURI = String.format("%s%s?apikey=%s", API_ENDPOINT, queryStocks, API_KEY);
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestURI))
                .header("accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        Object obj;

        logger.log(Level.INFO, "Requesting API endpoint FMP");
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // System.out.println(response.body());
            obj = new JSONParser().parse(response.body());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cant request API endpoint");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted");
            throw new RuntimeException(e);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "Could not parse to JSON");
            throw new RuntimeException(e);
        }
        return (JSONArray) obj;
    }

    public void extractWatchlistInfoFromJSONArray(JSONArray JSONstocks) throws FinancialPlannerException {
        if (JSONstocks.size() != stocks.size()) {
            throw new FinancialPlannerException("Error getting API info!");
        }
        long fetchTime = System.currentTimeMillis();
        for (Object jo : JSONstocks) {
            JSONObject stock = (JSONObject) jo;
            if (stocks.containsKey(stock.get("symbol").toString().toUpperCase())) {
                Stock stockLocal = stocks.get(stock.get("symbol").toString().toUpperCase());
                extractStockInfoFromJSONObject(stock, stockLocal, fetchTime);
            }
        }
    }

    public void extractStockInfoFromJSONObject(JSONObject stock, Stock stockLocal, long fetchTime) {
        stockLocal.setLastFetched(fetchTime);

        String price = stock.get("price").toString();
        assert price != null;
        stockLocal.setPrice(price);

        String exchange = stock.get("exchange").toString();
        assert exchange != null;
        stockLocal.setExchange(exchange);

        String dayHigh = stock.get("dayHigh").toString();
        assert dayHigh != null;
        stockLocal.setDayHigh(dayHigh);

        String dayLow = stock.get("dayLow").toString();
        assert dayLow != null;
        stockLocal.setDayLow(dayLow);

        String timestamp = stock.get("timestamp").toString();
        long lastUpdated = Long.parseLong(timestamp) * 1000;
        stockLocal.setLastUpdated(new Date(lastUpdated));
    }

    public String addStock(String stockCode) throws FinancialPlannerException {
        if (stocks.size() >= 5) {
            throw new FinancialPlannerException("Watchlist is full (max 5). Delete a stock to add a new one");
        }
        if (stocks.containsKey(stockCode.toUpperCase())) { // should already be uppercase
                throw new FinancialPlannerException("Stock is already present in Watchlist. Use watchlist to view it!");
        }

        Stock newStock;
        newStock = new Stock(stockCode);

        assert newStock.getSymbol() != null && newStock.getStockName() != null;
        stocks.put(newStock.getSymbol(), newStock);
        return newStock.getStockName();
    }

    public String deleteStock(String stockCode) throws FinancialPlannerException {
        if (stocks.isEmpty()) {
            throw new FinancialPlannerException("No stock in watchlist!");
        }
        Stock removedStock = stocks.remove(stockCode.toUpperCase()); // should be uppercase already
        if (removedStock == null) {
            throw new FinancialPlannerException("Does not Exist in Watchlist");
        }
        return removedStock.getStockName();
    }

    public int size() {
        return stocks.size();
    }

    public Stock get(int index) {
        return stocks.get(index);
    }

    public HashMap<String, Stock> getStocks() {
        return stocks;
    }

    public void setStocks(HashMap<String, Stock> stocks) {
        this.stocks = stocks;
    }
}
