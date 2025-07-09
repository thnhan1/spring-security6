package com.nhanab.accountservice.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResponseItemDto implements Serializable {
    Long id;
    String name;
    String description;
    String imageUrl;
    Integer price;
}
