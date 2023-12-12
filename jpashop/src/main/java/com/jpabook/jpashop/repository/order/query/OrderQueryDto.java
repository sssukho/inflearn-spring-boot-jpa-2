package com.jpabook.jpashop.repository.order.query;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "orderId")
public class OrderQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate; // 주문시간
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    // JPQL 로 짜더라도 컬렉션은 바로 넣어줄 수 없기 때문에 orderItems 는 생성자에서 빠졌다.

    public OrderQueryDto(final Long orderId, final String name, final LocalDateTime orderDate,
        final OrderStatus orderStatus, final Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
