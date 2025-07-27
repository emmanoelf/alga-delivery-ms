package com.algaworks.algadelivery.courier.management.domain.service;

import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourierDeliveryService {
    private final CourierRepository courierRepository;

    public void assign(UUID deliveryId){
        Courier courier = this.courierRepository.findTop1ByOrderByLastFulfilledDeliveryAtAsc().orElseThrow();
        courier.assign(deliveryId);
        this.courierRepository.saveAndFlush(courier);
        log.info("Courier {} assigned to delivery at {}", courier.getId(), deliveryId);
    }

    public void fulfill(UUID deliveryId){
        Courier courier = this.courierRepository.findByPendingDeliveries_id(deliveryId).orElseThrow();
        courier.fulfill(deliveryId);
        this.courierRepository.saveAndFlush(courier);
        log.info("Courier {} fulfilled the delivery {}", courier.getId(), deliveryId);
    }
}
