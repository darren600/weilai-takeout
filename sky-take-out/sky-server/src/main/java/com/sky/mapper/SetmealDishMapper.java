package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    * 根据菜品ID查询套餐ID
    * */
    //@Select("select setmeal_id from setmeal_dish where dish_id in (#{dishIds})
    List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);




    /*
    * 修改停售菜品的套餐状态
    * */
    void update(Setmeal setmeal);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐和菜品的关联关系-批量
     * @param setmealIds
     * @return
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /*
    * 根据套餐id查询菜品
    * */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

//    /*
//    * 根据套餐id删除套餐和菜品的关联关系
//    * */
//    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
//    void deleteBySetmealId(Long setmealId);
}

