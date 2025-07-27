package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryCheckpointService {

    private final DeliveryRepository deliveryRepository;

    public void place(UUID deliveryId){
        Delivery delivery = this.deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new DomainException("Id not found"));

        delivery.place();
        this.deliveryRepository.saveAndFlush(delivery);
    }

    public void pickUp(UUID deliveryId, UUID courierId){
        Delivery delivery = this.deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new DomainException("Id not found"));

        delivery.pickUp(courierId);
        this.deliveryRepository.saveAndFlush(delivery);
    }

    public void complete(UUID deliveryId){
        Delivery delivery = this.deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new DomainException("Id not found"));

        delivery.markAsDelivered(deliveryId);
        this.deliveryRepository.saveAndFlush(delivery);
    }

}
