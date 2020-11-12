package com.huobi.my;

import com.huobi.client.GenericClient;
import com.huobi.constant.HuobiOptions;

public class Tests {
    public static void main(String[] args){
        GenericClient genericService = GenericClient.create(HuobiOptions.builder().build());
        long startTime = System.currentTimeMillis();
        Long serverTime = genericService.getTimestamp();
        System.out.println("server time:" + serverTime);
        System.out.println(System.currentTimeMillis()-startTime);
    }
}
