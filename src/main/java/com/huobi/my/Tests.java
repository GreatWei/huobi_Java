package com.huobi.my;

import com.huobi.client.GenericClient;
import com.huobi.client.MarketClient;
import com.huobi.client.req.market.SubCandlestickRequest;
import com.huobi.client.req.market.SubMarketTradeRequest;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.enums.CandlestickIntervalEnum;
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
    public static void main(String[] args) throws InterruptedException {
        GenericClient genericService = GenericClient.create(HuobiOptions.builder().build());


        AtomicReference<BigDecimal> lastPrice = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicReference<BigDecimal> profit = new AtomicReference<BigDecimal>(new BigDecimal(0.000000));
        AtomicInteger count = new AtomicInteger(-30);
        AtomicBoolean signal = new AtomicBoolean(false);
        final  BigDecimal service_charge = new BigDecimal("0.002");
        AtomicReference<LinkedList<BigDecimal>> price_amountQueue = new AtomicReference<LinkedList<BigDecimal>>();
        AtomicReference<LinkedList<Long>> priceTime = new AtomicReference<LinkedList<Long>>();
        AtomicReference<LinkedList<BigDecimal>> amountTotal = new AtomicReference<LinkedList<BigDecimal>>();
        final long SystemTime = System.currentTimeMillis();
        MarketClient marketClient = MarketClient.create(new HuobiOptions());
        MarketClient marketClient15 = MarketClient.create(new HuobiOptions());
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//        date.setTime(time);
//        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(date));date
        String symbol = "btcusdt";
        marketClient.subCandlestick(SubCandlestickRequest.builder()
                .symbol(symbol)
                .interval(CandlestickIntervalEnum.DAY1)
                .build(), (candlestick) -> {
            date.setTime(candlestick.getTs());
            System.out.println(simpleDateFormat.format(date)+" "+candlestick.toString());
            System.out.println("===========================");
        });

        marketClient15.subCandlestick(SubCandlestickRequest.builder()
                .symbol(symbol)
                .interval(CandlestickIntervalEnum.MIN15)
                .build(), (candlestick) -> {
            date.setTime(candlestick.getTs());
            System.out.println(simpleDateFormat.format(date)+" "+candlestick.toString());
            System.out.println("===========================");
        });
        MarketClient marketClient1 = MarketClient.create(new HuobiOptions());
        Date date2 = new Date();
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        marketClient1.subMarketTrade(SubMarketTradeRequest.builder().symbol(symbol).build(), (marketTradeEvent) -> {
            date2.setTime(marketTradeEvent.getTs());

            BigDecimal min = new BigDecimal(999999999999999999999999.0);
            Long inputTime = marketTradeEvent.getTs();
            LinkedList<BigDecimal> input = new LinkedList<BigDecimal>();
            LinkedList<Long> blockTime = new LinkedList<Long>();
            LinkedList<BigDecimal> amount = new LinkedList<BigDecimal>();
            Long interarrivalTime = 120000L;
            for (MarketTrade marketTrade : marketTradeEvent.getList()) {
                input.offer(marketTrade.getPrice().multiply(marketTrade.getAmount()));
                blockTime.offer(marketTrade.getTs());
                amount.offer(marketTrade.getAmount());
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
                //  System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+marketTrade.toString());
            }
            BigDecimal averagePrice = average_price(amount,input, inputTime, blockTime, interarrivalTime, price_amountQueue, priceTime,amountTotal);
         //   log.error("min:" + min + ", averagePrice:" + averagePrice+","+((inputTime-SystemTime)>=interarrivalTime));
         //       System.err.println(min);
            if (count.get() > 0 && !signal.get() && min.compareTo(averagePrice) > 0&&(inputTime-SystemTime)>=interarrivalTime) {
                lastPrice.set(min);
                log.error("买入：" + min);
                signal.set(true);
                log.error("min:" + min + ", averagePrice:" + averagePrice);
            } else if (count.get() < 0 && signal.get() && min.compareTo(averagePrice) < 0) {
                log.error("卖出：" + min);
                profit.set(profit.get().add(min.subtract(lastPrice.get()).subtract(lastPrice.get().multiply(service_charge)).subtract(min.multiply(service_charge))));
                log.error("总盈利：" + profit.get());
                signal.set(false);
                log.error("min:" + min + ", averagePrice:" + averagePrice);
            }
            //  System.out.println(simpleDateFormat2.format(date2)+" "+"count:"+count.get()+" countVol:");
            //   System.out.println("-----------------------------");
        });


    }

    public synchronized static BigDecimal average_price(LinkedList<BigDecimal> amount,LinkedList<BigDecimal> input, Long inputTime, LinkedList<Long> blockTime, Long interarrivalTime, AtomicReference<LinkedList<BigDecimal>> price_amountQueue, AtomicReference<LinkedList<Long>> priceTime, AtomicReference<LinkedList<BigDecimal>> amountTotal) {

        if (price_amountQueue.get() == null || (price_amountQueue.get().peek() == null)) {
            price_amountQueue.set(input);
            priceTime.set(blockTime);
            amountTotal.set(amount);

            BigDecimal sum = new BigDecimal("0.0");
            for (BigDecimal price : input) {
                sum = sum.add(price);
            }
            BigDecimal N = new BigDecimal("0.0");
            for (BigDecimal price : amount) {
                N = N.add(price);
            }
            return sum.divide(N, 2, BigDecimal.ROUND_UP);
        } else {
            LinkedList<Long> time = priceTime.get();
            LinkedList<BigDecimal> price = price_amountQueue.get();
            LinkedList<BigDecimal> amountT= amountTotal.get();
            price.addAll(input);
            time.addAll(blockTime);
            amountT.addAll(amount);
            while (true) {
                Long tmp = time.peek();
                if(tmp==null){
                    time.poll();
                    price.poll();
                    amountT.poll();
                    continue;
                }
                if ((tmp + interarrivalTime) < inputTime) {
                    time.poll();
                    price.poll();
                    amountT.poll();
                } else {
                    break;
                }
            }
            BigDecimal sum = new BigDecimal("0.0");

            for (BigDecimal tmp : price) {

                sum = sum.add(tmp);
            }

            BigDecimal N = new BigDecimal("0.0");
            for (BigDecimal tmp : amountT) {
                N = N.add(tmp);
            }
            return sum.divide(N, 2, BigDecimal.ROUND_UP);
        }
    }

}
