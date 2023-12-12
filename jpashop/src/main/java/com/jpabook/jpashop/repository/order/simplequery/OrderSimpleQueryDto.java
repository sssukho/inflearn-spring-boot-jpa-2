package com.jpabook.jpashop.repository.order.simplequery;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.OrderStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderSimpleQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate; // 주문시간
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(final Long orderId, final String name, final LocalDateTime orderDate,
        final OrderStatus orderStatus, final Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
