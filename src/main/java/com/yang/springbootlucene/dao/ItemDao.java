package com.yang.springbootlucene.dao;

import com.yang.springbootlucene.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author YYX
 */
@Mapper
public interface ItemDao {

    @Select("SELECT \n" +
            "\tt.id AS id, \n" +
            "\tt.`name` AS `name`, \n" +
            "\tt.price AS price, \n" +
            "\tt.num AS num, \n" +
            "\tt.image AS image, \n" +
            "\tt.category_name AS categoryName, \n" +
            "\tt.brand_name AS brandName, \n" +
            "\tt.spec AS spec, \n" +
            "\tt.sale_num AS saleNum\n" +
            "FROM\n" +
            "\tt_item AS t")
    List<Item> listAll();
}
