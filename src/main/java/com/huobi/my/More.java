package com.huobi.my;

import com.huobi.client.MarketClient;
import com.huobi.client.req.market.CandlestickRequest;
import com.huobi.client.req.market.MarketTradeRequest;
import com.huobi.client.req.market.SubMarketTradeRequest;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.enums.CandlestickIntervalEnum;
import com.huobi.model.market.Candlestick;
import com.huobi.model.market.MarketTrade;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class More {
    public static void main(String[] args) throws InterruptedException {
        String symbol = "btcusdt";
        MarketClient marketClient = MarketClient.create(new HuobiOptions());
        DataFactor dataFactor = new DataFactor();
        MarketClient marketClient1 = MarketClient.create(new HuobiOptions());


//        marketClient1.subMarketTrade(SubMarketTradeRequest.builder().symbol(symbol).build(), (marketTradeEvent) -> {
//            System.out.println("ch:" + marketTradeEvent.getCh());
//            System.out.println("ts:" + marketTradeEvent.getTs());
//            marketTradeEvent.getList().forEach(marketTrade -> {
//                System.out.println(marketTrade.toString());
//            });
//
//            System.out.println("-----------------------------");
//        });

        long startTime;
        long endTime;
        boolean single = true;
        BigDecimal min = null;
        BigDecimal max = null;
        Candlestick k_day = null;
        BigDecimal buy = null;
        BigDecimal sell = null;
        BigDecimal sum = new BigDecimal("0.0");
        BigDecimal sub = null;
        final BigDecimal service_charge = new BigDecimal("0.002");

        while (true) {
            try {
                startTime = System.currentTimeMillis();
                List<Candlestick> listMIN15 = marketClient.getCandlestick(CandlestickRequest.builder()
                        .symbol(symbol)
                        .interval(CandlestickIntervalEnum.MIN15)
                        .size(50)
                        .build());
                List<Candlestick> listDAY1 = marketClient.getCandlestick(CandlestickRequest.builder()
                        .symbol(symbol)
                        .interval(CandlestickIntervalEnum.DAY1)
                        .size(1)
                        .build());
                k_day = listDAY1.get(0);

                double MA5 = dataFactor.MA(listMIN15, 5).get(4);
                double MA10 = dataFactor.MA(listMIN15, 10).get(9);
                double MA20 = dataFactor.MA(listMIN15, 20).get(19);
                if (single && MA5 > MA10) {
                    List<MarketTrade> marketTradeList = marketClient.getMarketTrade(MarketTradeRequest.builder().symbol(symbol).build());
                    for (int i = 0; i < marketTradeList.size(); i++) {
                        if (i == 0) {
                            min = marketTradeList.get(i).getPrice();
                        } else {
                            min = min.min(marketTradeList.get(i).getPrice());
                        }
                    }
                    if ((k_day.getHigh().multiply(service_charge).add(min.multiply(service_charge)).add(min)).compareTo(k_day.getHigh()) <= 0) {
                        log.error("buy:" + min);
                        buy = min;
                        single = false;
                    }
                } else if (!single && MA10 > MA5) {
                    List<MarketTrade> marketTradeList = marketClient.getMarketTrade(MarketTradeRequest.builder().symbol(symbol).build());
                    for (int i = 0; i < marketTradeList.size(); i++) {
                        if (i == 0) {
                            max = marketTradeList.get(i).getPrice();
                        } else {
                            max = max.max(marketTradeList.get(i).getPrice());
                        }
                    }
                    sub = max.subtract(buy).subtract(buy.multiply(service_charge)).subtract(max.multiply(service_charge));
                    if (sub.compareTo(new BigDecimal("0.0")) > 0) {
                        sell = max;
                        log.error("sell:" + sell);
                        sum = sum.add(sub);
                        log.error("profit:" + sum);
                        single = true;
                    }
                }
//            System.err.println(listDAY1.get(0));
//            System.err.println(MA5 + "," + MA10 + "," + MA20);
                endTime = System.currentTimeMillis();
                if ((endTime - startTime) > 100) {
                    continue;
                } else {
                    Thread.sleep(100-(endTime - startTime));
                }
            }catch (Exception e){
                log.error("Exception: "+e.toString());
            }

        }
    }
}
