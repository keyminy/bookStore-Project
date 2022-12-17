package com.meta.order.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.meta.cart.service.CartService;
import com.meta.cart.vo.CartVO;
import com.meta.config.auth.PrincipalDetails;
import com.meta.handler.exceptions.NotenoughCashException;
import com.meta.member.vo.MemberVO;
import com.meta.order.service.OrderService;
import com.meta.order.vo.OrderItemsVO;
import com.meta.order.vo.OrderVO;
import com.meta.util.dataUtil;

@Controller
@RequestMapping("/order")
public class OrderController {

	private static final Logger log = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;


	@GetMapping("list")
	public String order(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		log.info("order창");
		return "order/list";
	}


	@PostMapping("checkout")
	public String checkout(HttpServletRequest request, Model model) {
		String[] data = request.getParameterValues("cart_no");
		List<CartVO> list = new ArrayList<CartVO>();
		for (int i = 0; i < data.length; i++) {
			CartVO cartVo = new CartVO();
			cartVo.setCart_no(Integer.parseInt(data[i]));
			list.add(cartVo);
		}
		model.addAttribute("checkoutList", cartService.getCheckedoutCartList(list));
		model.addAttribute("sub_total_price", cartService.getSelectedSubTotalPrice(list));
		return "order/checkout";
	}

	@PostMapping("received")
	@Transactional
	public String received(OrderVO orderVo, HttpServletRequest request,
			@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
		MemberVO member = principalDetails.getMember();
		if(member.getCash() - orderVo.getOrder_price() < 0) {
			throw new NotenoughCashException("가지고 있는 돈이 부족합니다");
		}else {
			int calCash = member.getCash();
			calCash -= orderVo.getOrder_price();
			member.setCash(calCash);
			//세션값도 변경해주기
			principalDetails.setMember(member);
			//db에 적용해주기
			orderService.updateCash(member);
		}
		String[] cartData = request.getParameterValues("cart_no");
		orderService.insertOrder(orderVo);
		
		List<CartVO> cartList = new ArrayList<CartVO>();
		String order_no = orderVo.getOrder_no();
		
		int order_count = 0;
		
		for (int i = 0; i < cartData.length; i++) {
			CartVO cartVo = new CartVO();
			OrderItemsVO itemVo = new OrderItemsVO();
			int cart_no = Integer.parseInt(cartData[i]);
			cartVo = cartService.getACart(cart_no);
			itemVo.setOrder_no(order_no);
			itemVo.setBook_no(cartVo.getBook_no());
			itemVo.setOrder_qt(cartVo.getCart_book_qt());
			itemVo.setTotal_price(cartVo.getCart_total_price());
			if (orderService.insertOrderItem(itemVo) > 0) {
				//order_items 테이블에 각 cart값에 해당하는 정보 DB insert
				//Service에서 stockMapper.addStockWhenCheckedout()로 주문만큼의 재고 셋팅작업
				cartList.add(cartVo);
			}
			order_count+=cartVo.getCart_book_qt();
		}
		OrderVO orderCountVo = new OrderVO();
		orderCountVo.setOrder_count(order_count);//여기서 for문을 돌며 더해진 주문 수량 셋팅
		orderCountVo.setOrder_no(order_no);
		//orders테이블에 order_count Update
		orderService.setOrderCount(orderCountVo);
		
		if (cartService.deleteSelectedCart(cartList) > 0) {
			dataUtil.updateCartInfo(principalDetails, cartService);
		}

		model.addAttribute("orderItemsList", orderService.getOrderItems(order_no));
		model.addAttribute("orderInfo", orderService.getOrderInfo(order_no));

		return "order/order-received";
	}

	// 회원 주문 정보 확인 폼
	@GetMapping("myorder")
	public String showMyorder(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
		long m_no = principalDetails.getMember().getM_no();
		model.addAttribute("orderList", orderService.getOrderList(m_no));

		return "order/myorder";
	}

	// 회원 주문 정보 확인 폼
	@PostMapping("myorder/details")
	public String showMyorderDetails(String order_no, Model model,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		model.addAttribute("orderInfo", orderService.getOrderInfo(order_no));
		model.addAttribute("orderItems", orderService.getOrderItems(order_no));

		return "order/myorder-details";
	}

	@PostMapping("myorder/update")
	public String updateMyOrder(String order_no, Model model) {
		// orderService.deleteOrder(order_no);
		model.addAttribute("orderInfo", orderService.getOrderInfo(order_no));
		return "order/myorder-update";
	}

	@PostMapping("myorder/update/process")
	public String updateMyOrderProcess(OrderVO orderVo) {
		orderService.updateOrderInfo(orderVo);
		return "redirect:/order/myorder";
	}

	@PostMapping("myorder/delete")
	public String delteMyOrder(String order_no) {
		orderService.deleteOrder(order_no);
		return "redirect:/order/myorder";
	}
}
