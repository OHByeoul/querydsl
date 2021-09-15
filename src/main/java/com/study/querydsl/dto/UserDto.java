package com.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String name;
    private double age;

    @QueryProjection
    public UserDto(String name, double age) {
        this.name = name;
        this.age = age;
    }
}
