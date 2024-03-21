package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {


    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result SetStatus(@PathVariable int status){
        log.info("设置店铺的营业状态");
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    private Result<Integer> getStatus(){
        log.info("获取店铺的营业状态");
        Integer ststus = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        if(ststus == 1){
            log.info("营业中");
        }
        else{
            log.info("打样");
        }
        return Result.success(ststus);

    }

}
