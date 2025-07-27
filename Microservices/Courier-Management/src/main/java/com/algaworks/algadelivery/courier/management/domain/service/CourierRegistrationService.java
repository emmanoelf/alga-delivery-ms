package com.algaworks.algadelivery.courier.management.domain.service;

import com.algaworks.algadelivery.courier.management.api.model.CourierInput;
import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CourierRegistrationService {
    private final CourierRepository courierRepository;

    public Courier create(CourierInput input) {
        Courier courier = Courier.brandNew(input.getName(), input.getPhone());
        return this.courierRepository.saveAndFlush(courier);
    }

    public Courier update(UUID courierId, CourierInput input) {
        Courier courier = this.courierRepository.findById(courierId).orElseThrow();
        courier.setName(input.getName());
        courier.setPhone(input.getPhone());
        return this.courierRepository.saveAndFlush(courier);
    }
}
