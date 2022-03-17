package VO;

import com.worm.seckill.entity.Goods;
import lombok.Data;

import java.util.Date;

@Data
public class GoodsVO extends Goods {
	private Double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
}
