package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> pagingSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> pagingComplicate(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> paingOptimize(MemberSearchCondition condition, Pageable pageable);
}
