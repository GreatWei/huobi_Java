package com.huobi.my;

import com.huobi.client.AccountClient;
import com.huobi.client.GenericClient;
import com.huobi.client.MarketClient;
import com.huobi.client.TradeClient;
import com.huobi.client.req.account.AccountBalanceRequest;
import com.huobi.client.req.trade.CreateOrderRequest;
import com.huobi.client.req.wallet.CreateWithdrawRequest;
import com.huobi.constant.Constants;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.enums.OrderSourceEnum;
import com.huobi.constant.enums.OrderTypeEnum;
import com.huobi.model.account.Account;
import com.huobi.model.account.AccountBalance;
import com.huobi.model.account.Balance;
import com.huobi.model.generic.Symbol;
import com.huobi.service.huobi.HuobiWalletService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Test1 {



    public static void main(String[] args) {
        // Create a GenericClient instance
        GenericClient genericService = GenericClient.create(new HuobiOptions());

        // Create a MarketClient instance
        MarketClient marketClient = MarketClient.create(new HuobiOptions());


        // Create an AccountClient instance with APIKey
        AccountClient accountService = AccountClient.create(HuobiOptions.builder()
                .apiKey(Constants.API_KEY)
                .secretKey(Constants.SECRET_KEY)
                .build());
        Account account = accountService.getAccounts().get(0);
        AccountBalanceRequest request = new AccountBalanceRequest();
        request.setAccountId(account.getId());
        AccountBalance accountBalance = accountService.getAccountBalance(request);
        for(Balance balance:accountBalance.getList()){
            if("usdt".equals(balance.getCurrency())&&"trade".equals(balance.getType())){
                System.err.println(balance);
            }
        }
        System.err.println(accountBalance.toString());
       System.err.println(account);
        // Create a TradeClient instance with API Key
        TradeClient tradeService = TradeClient.create(HuobiOptions.builder()
                .apiKey(Constants.API_KEY)
                .secretKey(Constants.SECRET_KEY)
                .build());
        String symbol="btcusdt";
        String clientOrderId= UUID.randomUUID().toString();
//        CreateOrderRequest buyLimitRequest = new CreateOrderRequest();
//        buyLimitRequest.setSymbol(symbol);
//        buyLimitRequest.setAccountId(account.getId());
//        buyLimitRequest.setAmount(new BigDecimal("5"));
//      //  buyLimitRequest.setPrice(new BigDecimal("10"));
//        buyLimitRequest.setClientOrderId(clientOrderId);
//        buyLimitRequest.setType(OrderTypeEnum.BUY_MARKET);
//        buyLimitRequest.setSource("spot-api");
//        buyLimitRequest.setOrderSource(OrderSourceEnum.SPOT_API);
//        Long buyLimitId = tradeService.createOrder(buyLimitRequest);

        BigDecimal price = new BigDecimal("10");
        BigDecimal amount = new BigDecimal("0.5");
        CreateOrderRequest buyLimitRequest = CreateOrderRequest.spotBuyLimit(account.getId(), clientOrderId, symbol,price ,amount);
//        Long buyLimitId = tradeService.createOrder(buyLimitRequest);
//
//       System.err.println(buyLimitId);
        List<Symbol> symbolList = genericService.getSymbols();
        for(Symbol symbol1:symbolList){
            if("btcusdt".equals(symbol1.getSymbol()))
            System.err.println(symbol1.toString());
        }
        List<String> currencyList = genericService.getCurrencys();
        for(String s:currencyList){
        //    System.err.println(s);
        }

        HuobiWalletService walletService = new HuobiWalletService(HuobiOptions.builder()
                .apiKey(Constants.API_KEY)
                .secretKey(Constants.SECRET_KEY)
                .build());


    }
}
