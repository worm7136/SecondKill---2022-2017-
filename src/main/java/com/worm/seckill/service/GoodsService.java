package com.worm.seckill.service;

import VO.GoodsVO;
import com.worm.seckill.entity.Goods;
import com.worm.seckill.entity.MiaoshaGoods;
import com.worm.seckill.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    //查询全部商品
    public List<GoodsVO> goodsVOList(){
        return goodsMapper.listGoodsVO();
    }

    //通过商品id查询商品
    public GoodsVO getGoodsVOByGoodsId(Long goodsId){
        return goodsMapper.getGoodsVOByGoodsId(goodsId);
    }

    //减库存
    public int reduceStock(GoodsVO goods){
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        //标记这条sql是否执行成功，0不成功，1成功
        return goodsMapper.reduceStock(g);
    }

    public void resetStock(List<GoodsVO> goodsList) {
        for(GoodsVO goods : goodsList ) {
            MiaoshaGoods g = new MiaoshaGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsMapper.resetStock(g);
        }
    }

}
