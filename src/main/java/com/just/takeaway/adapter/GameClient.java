package com.just.takeaway.adapter;

import com.just.takeaway.controller.GameApi;
import com.just.takeaway.fallback.KafkaFallback;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "game-of-three", url = "${feign.url}", fallback = KafkaFallback.class)
public interface GameClient extends GameApi {


}
