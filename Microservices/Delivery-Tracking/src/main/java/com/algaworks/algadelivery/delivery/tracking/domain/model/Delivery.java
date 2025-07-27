package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectedDeliveredAt;
    private OffsetDateTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="zipCode", column = @Column(name = "sender_zip_code")),
            @AttributeOverride(name="street", column = @Column(name = "sender_street")),
            @AttributeOverride(name="number", column = @Column(name = "sender_number")),
            @AttributeOverride(name="complement", column = @Column(name = "sender_complement")),
            @AttributeOverride(name="name", column = @Column(name = "sender_name")),
            @AttributeOverride(name="phone", column = @Column(name = "sender_phone"))
    })
    private ContactPoint sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="zipCode", column = @Column(name = "recipient_zip_code")),
            @AttributeOverride(name="street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name="number", column = @Column(name = "recipient_number")),
            @AttributeOverride(name="complement", column = @Column(name = "recipient_complement")),
            @AttributeOverride(name="name", column = @Column(name = "recipient_name")),
            @AttributeOverride(name="phone", column = @Column(name = "recipient_phone"))
    })
    private ContactPoint recipient;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "delivery")
    private List<Item> items = new ArrayList<>();

    public static Delivery draft(){
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);

        return delivery;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public UUID addItem(String name, int quantity){
        Item item = Item.brandNew(name, quantity, this);
        this.items.add(item);
        this.calculateTotalItems();
        return item.getId();
    }

    public void removeItem(UUID itemId){
        this.items.removeIf(item -> item.getId().equals(itemId));
        this.calculateTotalItems();
    }

    public void removeItems(){
        this.items.clear();
        this.calculateTotalItems();
    }

    public void changeItemQuantity(UUID itemId, int quantity){
        Item item = this.getItems().stream().filter(itemQtd -> itemQtd.getId().equals(itemId))
                .findFirst().orElseThrow();
        item.setQuantity(quantity);
        this.calculateTotalItems();
    }

    public void editPreparationDetails(PreparationDetails preparationDetails){
        this.verifyIfCanBeEdited();
        this.setSender(preparationDetails.getSender());
        this.setRecipient(preparationDetails.getRecipient());
        this.setDistanceFee(preparationDetails.getDistanceFee());
        this.setCourierPayout(preparationDetails.getCourierPayout());
        this.setExpectedDeliveredAt(OffsetDateTime.now().plus(preparationDetails.getExpectedDeliveryTime()));
        this.setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }

    public void place(){
        this.verifyIfCanBePlaced();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
    }

    public void pickUp(UUID courierId){
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
    }

    public void markAsDelivered(UUID courierId){
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
    }

    private void calculateTotalItems(){
        int totalItems = this.getItems().stream().mapToInt(Item::getQuantity).sum();
        this.setTotalItems(totalItems);
    }

    private void verifyIfCanBeEdited(){
        if(!this.getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException("Delivery cannot be edited");
        }
    }

    private void verifyIfCanBePlaced(){
        if(!this.isFilled()){
            throw new DomainException();
        }
        if(!this.getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private boolean isFilled(){
        return this.getSender() != null
                && this.getRecipient() != null
                && this.getTotalCost() != null;
    }

    private void changeStatusTo(DeliveryStatus newStatus){
        if(newStatus != null && this.getStatus().canNotChangeTo(newStatus)){
            throw new DomainException("Invalid status transition from " + this.getStatus() + " to " + newStatus);
        }
        this.setStatus(newStatus);
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PreparationDetails{
        private ContactPoint sender;
        private ContactPoint recipient;
        private BigDecimal distanceFee;
        private BigDecimal courierPayout;
        private Duration expectedDeliveryTime;
    }
}
