package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryTest {

    @Test
    public void shouldChangeToPlaced(){
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(this.createValidPreparationDetails());

        delivery.place();

        assertEquals(DeliveryStatus.WAITING_FOR_COURIER, delivery.getStatus());
        assertNotNull(delivery.getPlacedAt());
    }

    @Test
    public void shouldNotPlace(){
        Delivery delivery = Delivery.draft();

        assertThrows(DomainException.class, () ->{
            delivery.place();
        });

        assertEquals(DeliveryStatus.DRAFT, delivery.getStatus());
        assertNull(delivery.getPlacedAt());
    }

    private Delivery.PreparationDetails createValidPreparationDetails(){
        ContactPoint sender = ContactPoint.builder()
                .zipCode("12345-666")
                .street("Rua Porto Alegre")
                .number("123")
                .complement("Apto 745")
                .name("João Pedro")
                .phone("(51) 93333-6587")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("98765-432")
                .street("Rua Letrados")
                .number("456")
                .complement("")
                .name("Maria")
                .phone("(51) 94444-4587")
                .build();

        return Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("7.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }
}