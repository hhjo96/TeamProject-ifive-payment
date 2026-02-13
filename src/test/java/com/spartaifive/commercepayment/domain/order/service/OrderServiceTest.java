package com.spartaifive.commercepayment.domain.order.service;

import com.spartaifive.commercepayment.domain.order.customexception.ProductsNotAvailableException;
import com.spartaifive.commercepayment.domain.order.dto.request.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@TestPropertySource(properties = {"spring.config.additional-location= classpath:test-h2-basic.yml"})
public class OrderServiceTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @Test
    @Transactional
    public void 단종이나_품절_상품은_결제_불가() {
        // given
        MembershipGrade membership = MembershipGrade.builder()
                .id(1L)
                .name("NORMAL")
                .rate(1L)
                .build();

        User user = User.create(
                membership,
                "momo",
                "momo@gmail.com",
                "password",
                "01011112222");

        List<Product> products = List.of(
                new Product("포카칩", new BigDecimal("1000"), 10L, ProductStatus.ON_SALE, ProductCategory.FOOD, "맛있는 감자칩"),
                new Product("배터리", new BigDecimal("2000"), 1L, ProductStatus.ON_SALE, ProductCategory.ELECTRONICS, "AA 배터리"),
                new Product("요요", new BigDecimal("1500"), 5L, ProductStatus.DISCONTINUED ,ProductCategory.TOY, "재밌는 요요"),
                new Product("모자", new BigDecimal("1500"), 3L, ProductStatus.ON_SALE, ProductCategory.CLOTHES, "그냥 모자"),
                // 일부러 손수건은 재고가 다 떨어져 있지 않은 상태에서 OUT_OF_STOCK입니다.
                new Product("손수건", new BigDecimal("3000"), 3L, ProductStatus.OUT_OF_STOCK, ProductCategory.CLOTHES, "예쁜 손수거"), 
                new Product("새우깡", new BigDecimal("1200"), 0L, ProductStatus.OUT_OF_STOCK, ProductCategory.FOOD, "새우맛 과자")
        );

        Order order = new Order(BigDecimal.valueOf(1000), user);

        List<OrderProduct> orderProducts = new ArrayList<>();

        for (Product p : products) {
            orderProducts.add(new OrderProduct(order, p, 1L));
        }

        user = userRepository.save(user);
        productRepository.saveAll(products);
        orderRepository.save(order);
        orderProducts = orderProductRepository.saveAll(orderProducts);

        AddOrderRequest addOrderRequest = new AddOrderRequest(
                orderProducts.stream().map(x -> new AddOrderRequest.RequestProduct(x.getId(), 1L)).toList()
        );

        // when then
        try {
            orderService.addOrder(addOrderRequest, user.getId());
        } catch (ProductsNotAvailableException e) {
            assertThat(e.getUnAvailableProductIds()).containsExactlyInAnyOrder(3L, 5L, 6L);
        }
    }
}
