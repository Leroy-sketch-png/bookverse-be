package com.example.bookverseserver.exception;

import com.example.bookverseserver.dto.response.Order.UnavailableItemDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class OutOfStockException extends RuntimeException {
  private final List<UnavailableItemDTO> unavailableItems;

  public OutOfStockException(List<UnavailableItemDTO> unavailableItems) {
    super("Some items are out of stock");
    this.unavailableItems = unavailableItems;
  }
}
