package com.spartaifive.commercepayment.domain.order.service;

import com.spartaifive.commercepayment.domain.order.dto.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.AddOrderResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.order.util.OrderSupport;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    // TODO: 현재는 단종된 상품 뿐만 아니라 품절된 상품도 주문 가능 합니다.
    @Transactional
    public AddOrderResponse addOrder(AddOrderRequest req) {
        req = OrderSupport.NormalizeAddOrderRequest(req);

        Map<Long, AddOrderRequest.RequestProduct> productIdToReq = new HashMap<>();
        for (AddOrderRequest.RequestProduct reqP : req.getOrderProducts()) {
            Long id = reqP.getProductId();
            if (id != null) {
                productIdToReq.put(id, reqP);
            }
        }

        // 요청에서 들어온 상품 들을 조회
        List<Product> products = productRepository.findAllById(productIdToReq.keySet());

        // 총 금액을 계산
        BigDecimal total = BigDecimal.ZERO;

        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();

            if (quantity != null) {
                total = total.add(p.getPrice().multiply(new BigDecimal(quantity)));
            }
        }

        // 주문 객체 생성
        // TODO: 현재 저희에게는 유저의 개념이 없기 때문에 일단은 0을 저장합니다.
        Order order = new Order(total, 0L);

        // 주문 상품 생성
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();

            if (quantity != null) {
                OrderProduct orderProduct = new OrderProduct(
                    order, p, quantity
                );
                orderProducts.add(orderProduct);
            }
        }

        order = orderRepository.saveAndFlush(order);
        orderProducts = orderProductRepository.saveAllAndFlush(orderProducts);

        AddOrderResponse res = AddOrderResponse.fromOrderAndOrderProducts(
                order,
                orderProducts);

        return res;
    }
}
