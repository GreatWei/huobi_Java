package com.huobi.my;

import com.huobi.client.MarketClient;
import com.huobi.client.req.market.*;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.enums.CandlestickIntervalEnum;
import com.huobi.constant.enums.DepthLevels;
import com.huobi.constant.enums.DepthSizeEnum;
import com.huobi.constant.enums.DepthStepEnum;
import com.huobi.model.market.*;
import com.tictactec.ta.lib.Core;

import java.util.ArrayList;
import java.util.List;

public class MarketClientExample {

    public static void main(String[] args) {

        MarketClient marketClient = MarketClient.create(new HuobiOptions());
        DataFactor dataFactor = new DataFactor();
        String symbol = "btcusdt";

        List<Candlestick> list = marketClient.getCandlestick(CandlestickRequest.builder()
                .symbol(symbol)
                .interval(CandlestickIntervalEnum.MIN15)
                .size(20)
                .build());
        long TIME = System.currentTimeMillis();
        System.err.println(dataFactor.MA(list, 5));
        System.err.println(System.currentTimeMillis() - TIME);
      TIME = System.currentTimeMillis();
        System.err.println(dataFactor.MA(list, 10));
      System.err.println(System.currentTimeMillis() - TIME);
      TIME = System.currentTimeMillis();
        System.err.println(dataFactor.MA(list, 20));
      System.err.println(System.currentTimeMillis() - TIME);
        Core core = new Core();

//    MarketDetailMerged marketDetailMerged = marketClient.getMarketDetailMerged(MarketDetailMergedRequest.builder().symbol(symbol).build());
//    System.out.println(marketDetailMerged.toString());
//
//    List<MarketTicker> tickerList = marketClient.getTickers();
//    tickerList.forEach(marketTicker -> {
//      System.out.println(marketTicker.toString());
//    });
//
//    MarketDepth marketDepth = marketClient.getMarketDepth(MarketDepthRequest.builder()
//        .symbol(symbol)
//        .depth(DepthSizeEnum.SIZE_5)
//        .step(DepthStepEnum.STEP0)
//        .build());
//
//    System.out.println(marketDepth.toString());
//
//    marketDepth.getBids().forEach(priceLevel -> {
//      System.out.println("bid: " + priceLevel);
//    });
//    System.out.println("----------------------------");
//    marketDepth.getAsks().forEach(priceLevel -> {
//      System.out.println("asks: " + priceLevel);
//    });
//
//    List<MarketTrade> marketTradeList = marketClient.getMarketTrade(MarketTradeRequest.builder().symbol(symbol).build());
//    marketTradeList.forEach(marketTrade -> {
//      System.out.println(marketTrade.toString());
//    });
//
//    List<MarketTrade> marketHistoryTradeList = marketClient.getMarketHistoryTrade(MarketHistoryTradeRequest.builder().symbol(symbol).build());
//    marketHistoryTradeList.forEach(marketTrade -> {
//      System.out.println(marketTrade.toString());
//    });
//
//    MarketDetail marketDetail = marketClient.getMarketDetail(MarketDetailRequest.builder().symbol(symbol).build());
//    System.out.println(marketDetail.toString());
//
//    marketClient.subCandlestick(SubCandlestickRequest.builder()
//        .symbol(symbol)
//        .interval(CandlestickIntervalEnum.MIN15)
//        .build(), (candlestick) -> {
//
//      System.out.println(candlestick.toString());
//    });
//
//    marketClient.subMarketDetail(SubMarketDetailRequest.builder().symbol(symbol).build(), (marketDetailEvent) -> {
//      System.out.println(marketDetailEvent.toString());
//    });
//
//    marketClient.subMarketDepth(SubMarketDepthRequest.builder().symbol(symbol).build(), (marketDetailEvent) -> {
//      System.out.println(marketDetailEvent.toString());
//    });
//
//    marketClient.subMarketTrade(SubMarketTradeRequest.builder().symbol(symbol).build(), (marketTradeEvent) -> {
//
//      System.out.println("ch:" + marketTradeEvent.getCh());
//      System.out.println("ts:" + marketTradeEvent.getTs());
//
//      marketTradeEvent.getList().forEach(marketTrade -> {
//        System.out.println(marketTrade.toString());
//      });
//
//      System.out.println("-----------------------------");
//    });
//
//    marketClient.subMarketBBO(SubMarketBBORequest.builder().symbol(symbol).build(), (marketBBOEvent) -> {
//      System.out.println(marketBBOEvent.toString());
//    });
//
//    marketClient.reqCandlestick(ReqCandlestickRequest.builder()
//        .symbol(symbol)
//        .interval(CandlestickIntervalEnum.MIN15)
//        .build(), candlestickReq -> {
//
//      System.out.println(candlestickReq.toString());
//      candlestickReq.getCandlestickList().forEach(candlestick -> {
//        System.out.println("candlestick:" + candlestick.toString());
//      });
//    });
//
//    marketClient.reqMarketDepth(ReqMarketDepthRequest.builder()
//        .symbol(symbol)
//        .step(DepthStepEnum.STEP0)
//        .build(), marketDepthReq -> {
//
//      System.out.println(marketDepthReq.toString());
//    });
//
//    marketClient.reqMarketTrade(ReqMarketTradeRequest.builder()
//        .symbol(symbol)
//        .build(), marketTradeReq -> {
//
//      System.out.println(marketTradeReq.toString());
//    });
//
//    marketClient.reqMarketDetail(ReqMarketDetailRequest.builder()
//        .symbol(symbol)
//        .build(), marketDetailReq -> {
//
//      System.out.println(marketDetailReq.toString());
//    });
//
//    marketClient.subMbpRefreshUpdate(SubMbpRefreshUpdateRequest.builder().symbols(symbol).levels(DepthLevels.LEVEL_5).build(), event -> {
//      System.out.println(event.toString());
//    });
//
//    marketClient.subMbpIncrementalUpdate(SubMbpIncrementalUpdateRequest.builder().symbol(symbol).build(), event->{
//      System.out.println(event.toString());
//    });


    }


}
