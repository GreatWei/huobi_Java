package com.huobi.my;

import com.huobi.client.GenericClient;
import com.huobi.client.MarketClient;
import com.huobi.client.req.market.SubMarketTradeRequest;
import com.huobi.constant.HuobiOptions;
import com.huobi.model.market.MarketTrade;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.*;
@Slf4j
public class Tests {
    public static void main(String[] args) {
        GenericClient genericService = GenericClient.create(HuobiOptions.builder().build());
//        for(int i=0;i<10;i++){
//            System.out.println(i);
//            long startTime = System.currentTimeMillis();
//            Long serverTime = genericService.getTimestamp();
//            System.out.println("server time:" + serverTime);
//            SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            System.out.println("server time:" + format.format(serverTime));
//            System.out.println(System.currentTimeMillis()-startTime);
//        }

        AtomicReference<BigDecimal> lastPrice = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicReference<BigDecimal> profit = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicInteger count = new AtomicInteger(-30);
        AtomicBoolean signal = new AtomicBoolean(false);
        MarketClient marketClient = MarketClient.create(new HuobiOptions());
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//        date.setTime(time);
//        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(date));date
        String symbol = "btcusdt";
//        marketClient.subCandlestick(SubCandlestickRequest.builder()
//                .symbol(symbol)
//                .interval(CandlestickIntervalEnum.MIN1)
//                .build(), (candlestick) -> {
//            date.setTime(candlestick.getTs());
//            System.out.println(simpleDateFormat.format(date)+" "+candlestick.toString());
//            System.out.println("===========================");
//        });
        MarketClient marketClient1 = MarketClient.create(new HuobiOptions());
        Date date2 = new Date();
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        marketClient1.subMarketTrade(SubMarketTradeRequest.builder().symbol(symbol).build(), (marketTradeEvent) -> {
            date2.setTime(marketTradeEvent.getTs());
            boolean power = false;
            int countTmp = 0;
//            marketTradeEvent.getList().forEach(marketTrade -> {
//                if("sell".equals(marketTrade.getDirection())){
//                        tmpCount.getAndDecrement();
//                }else {
//                    tmpCount.getAndDecrement();
//                }
//                System.out.println(simpleDateFormat2.format(date2)+" "+"count:"+count.get()+" countVol:"+countVol.get());
//                System.out.println(simpleDateFormat2.format(date2)+" "+marketTrade.toString());
//            });
            BigDecimal min = new BigDecimal(999999999999999999999999.0);
            for (MarketTrade marketTrade : marketTradeEvent.getList()) {
                if ("sell".equals(marketTrade.getDirection())) {
                    int p = count.decrementAndGet();
                    if (p < -100) {
                        count.set(-30);
                    }
                } else {
                    int p = count.incrementAndGet();
                    if (p > 100) {
                        count.set(29);
                    }
                }
                if (min.compareTo(marketTrade.getPrice()) >= 0) {
                    min = marketTrade.getPrice();
                }
                //    System.out.println(simpleDateFormat2.format(date2)+" "+marketTrade.toString());

            }
            //    System.err.println(min);
            if (count.get() > 0 && !signal.get()) {
                lastPrice.set(min);
                log.error("买入：" + min);
                signal.set(true);
            } else if (count.get() < 0 && signal.get()) {
                log.error("卖出：" + min);
                profit.set(profit.get().add(min.subtract(lastPrice.get())));
                log.error("盈利：" + profit.get());
                signal.set(false);
            }
              //  System.out.println(simpleDateFormat2.format(date2)+" "+"count:"+count.get()+" countVol:");
            //   System.out.println("-----------------------------");
        });
    }

}
