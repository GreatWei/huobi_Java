package com.huobi.my;

import com.huobi.client.GenericClient;
import com.huobi.client.MarketClient;
import com.huobi.client.req.market.ReqCandlestickRequest;
import com.huobi.client.req.market.SubCandlestickRequest;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.enums.CandlestickIntervalEnum;

import java.text.SimpleDateFormat;

public class Tests {
    public static void main(String[] args) {
        GenericClient genericService = GenericClient.create(HuobiOptions.builder().build());
        for(int i=0;i<10;i++){
            System.out.println(i);
            long startTime = System.currentTimeMillis();
            Long serverTime = genericService.getTimestamp();
            System.out.println("server time:" + serverTime);
            SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("server time:" + format.format(serverTime));
            System.out.println(System.currentTimeMillis()-startTime);
        }

        MarketClient marketClient = MarketClient.create(new HuobiOptions());

        String symbol = "btcusdt";
//        marketClient.subCandlestick(SubCandlestickRequest.builder()
//                .symbol(symbol)
//                .interval(CandlestickIntervalEnum.MIN1)
//                .build(), (candlestick) -> {
//
//            System.out.println(candlestick.toString());
//        });
    }

}
