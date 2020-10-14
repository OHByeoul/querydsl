package com.study.querydsl.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter @Getter
public class Team {
    @Id @GeneratedValue
    private Long id;

    @OneToMany(mappedBy="team")
    private List<Member> members = new ArrayList<>();
}
