package com.huobi.my;

import com.huobi.client.AccountClient;
import com.huobi.client.GenericClient;
import com.huobi.client.MarketClient;
import com.huobi.client.TradeClient;
import com.huobi.client.req.account.AccountBalanceRequest;
import com.huobi.client.req.wallet.CreateWithdrawRequest;
import com.huobi.constant.Constants;
import com.huobi.constant.HuobiOptions;
import com.huobi.model.account.Account;
import com.huobi.model.account.AccountBalance;
import com.huobi.model.account.Balance;
import com.huobi.model.generic.Symbol;
import com.huobi.service.huobi.HuobiWalletService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

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





        Long serverTime = genericService.getTimestamp();
       System.err.println(String.valueOf(serverTime));
        List<Symbol> symbolList = genericService.getSymbols();
        for(Symbol symbol:symbolList){
           // System.err.println(symbol.toString());
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
