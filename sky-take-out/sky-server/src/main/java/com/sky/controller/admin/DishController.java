package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
* 菜品管理
* */
@RestController
@RequestMapping("/admin/dish")
@Tag(name = "菜品管理")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
   /*
   * 新增菜品
   * */
    @PostMapping
    @Operation(summary = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
//        清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
//        精确清理缓存数据
        clearAllCache(key);
        return Result.success();

    }
    /*
    * 分页查询菜品
    * */
    @GetMapping("/page")
    @Operation(summary = "分页查询菜品")
    public Result<PageResult> Page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
     * 菜品批量删除
     * */
    @DeleteMapping
    @Operation(summary = "批量删除")//MVC-@RequestParam将前端请求的参数的字符串解析成list数组
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
//        因为删除的菜品可能在不同的分类上，所以直接清理所有菜品缓存数据
//        获取所有匹配的keys，删除支持集合
        clearAllCache("dish_*");
        return Result.success();
    }
    /*
    * 根据ID查询菜品
    * */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据ID查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }
    /*
    * 修改菜品
    * */
    @PutMapping
    @Operation(summary = "修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
//        将所有的菜品缓存数据清掉，所有以dish_开头的key，不会经常做出修改，为了不必要的麻烦，直接清理所有菜品缓存数据
        clearAllCache("dish_*");
        return Result.success();
    }

    /*
     * 菜品起售停售
     * */

    @PostMapping("/status/{status}")
    @Operation(summary = "菜品起售停售")
    public Result updateStatus(@PathVariable Integer status, Long id){
        log.info("菜品起售停售：{},{}",status,id);
        dishService.updateStatus(status,id);
        //        将所有的菜品缓存数据清掉，所有以dish_开头的key
//        注：如果单独通过菜品id获得菜品数据，再获得分类id，这样子也要查询数据库，没有必要
        clearAllCache("dish_*");
        return Result.success();
    }
    /*
    * 根据分类id查询菜品
    * */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> dish = dishService.list(categoryId);
        return Result.success(dish);
    }
    /*
    * 清理缓存数据
    * */
    private void clearAllCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }
}
