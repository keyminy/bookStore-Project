package com.meta.book.vo;

import com.meta.book.constant.ItemSellStatus;

import lombok.Data;

@Data
public class BookVO {
	private Long book_no;
	private String title;
	private String author;
	private String publisher;
	private String pubdate;
	private String description;
	private int price;
	private int cate_no;
	private String image;
	private String th_image;
	private int stock;
	private int rownum;
	private ItemSellStatus itemSellStatus;//상품 판매 상태 enum
}
