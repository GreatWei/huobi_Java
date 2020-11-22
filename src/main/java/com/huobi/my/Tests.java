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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.*;
/**
 *
 *
 *
 * ps -aux | grep "huobi-client-2.0.3-SNAPSHOT.jar"
 * ps -aux | grep robot
 *
 * nohup java -jar huobi-client-2.0.3-SNAPSHOT.jar &
 *
 *
 *
 * */
@Slf4j
public class Tests {
    public static void main(String[] args) {
        GenericClient genericService = GenericClient.create(HuobiOptions.builder().build());


        AtomicReference<BigDecimal> lastPrice = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicReference<BigDecimal> profit = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicInteger count = new AtomicInteger(-30);
        AtomicBoolean signal = new AtomicBoolean(false);

        AtomicReference<LinkedList<BigDecimal>> priceQueue = new AtomicReference<LinkedList<BigDecimal>>();
        AtomicReference<LinkedList<Long>> priceTime = new AtomicReference<LinkedList<Long>>();

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

            BigDecimal min = new BigDecimal(999999999999999999999999.0);
            Long inputTime = marketTradeEvent.getTs();
            LinkedList<BigDecimal> input = new LinkedList<BigDecimal>();
            LinkedList<Long> blockTime = new LinkedList<Long>();
            Long interarrivalTime = 4000L;
            for (MarketTrade marketTrade : marketTradeEvent.getList()) {
                input.offer(marketTrade.getPrice());
                blockTime.offer(marketTrade.getTs());
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
               //   System.err.println(marketTrade.toString());
            }
            BigDecimal averagePrice = average_price(input, inputTime, blockTime, interarrivalTime, priceQueue, priceTime);
         //   log.error("min:" + min + ", averagePrice:" + averagePrice);
            //    System.err.println(min);
            if (count.get() > 0 && !signal.get() && min.compareTo(averagePrice) > 0) {
                lastPrice.set(min);
                log.error("买入：" + min);
                signal.set(true);
                log.error("min:" + min + ", averagePrice:" + averagePrice);
            } else if (count.get() < 0 && signal.get() && min.compareTo(averagePrice) < 0) {
                log.error("卖出：" + min);
                profit.set(profit.get().add(min.subtract(lastPrice.get())));
                log.error("盈利：" + profit.get());
                signal.set(false);
                log.error("min:" + min + ", averagePrice:" + averagePrice);
            }
            //  System.out.println(simpleDateFormat2.format(date2)+" "+"count:"+count.get()+" countVol:");
            //   System.out.println("-----------------------------");
        });
    }

    public synchronized static BigDecimal average_price(LinkedList<BigDecimal> input, Long inputTime, LinkedList<Long> blockTime, Long interarrivalTime, AtomicReference<LinkedList<BigDecimal>> priceQueue, AtomicReference<LinkedList<Long>> priceTime) {

        if (priceQueue.get() == null || (priceQueue.get().peek() == null)) {
            priceQueue.set(input);
            priceTime.set(blockTime);
            int N = 0;
            BigDecimal sum = new BigDecimal("0.0");
            for (BigDecimal price : input) {
                N++;
                sum = sum.add(price);
            }
            return sum.divide(new BigDecimal(N==0?1:N), 2, BigDecimal.ROUND_UP);
        } else {
            LinkedList<Long> time = priceTime.get();
            LinkedList<BigDecimal> price = priceQueue.get();

            price.addAll(input);
            time.addAll(blockTime);

            while (true) {
                Long tmp = time.peek();
                if(tmp==null){
                    time.poll();
                    price.poll();
                    continue;
                }
                if ((tmp + interarrivalTime) < inputTime) {
                    time.poll();
                    price.poll();
                } else {
                    break;
                }
            }
            BigDecimal sum = new BigDecimal("0.0");
            int N = 0;
            for (BigDecimal tmp : price) {
                N++;
                sum = sum.add(tmp);
            }
            return sum.divide(new BigDecimal(N==0?1:N), 2, BigDecimal.ROUND_UP);
        }
    }

}
