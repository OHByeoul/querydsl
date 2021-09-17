package com.study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.dto.QMemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;


public class MemberRepositoryImpl implements  MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public BooleanExpression usernameEq(String username){
        return StringUtils.isEmpty(username) ? null : member.username.eq(username);
    }

    public BooleanExpression teamnameEq(String teamname) {
        return StringUtils.isEmpty(teamname) ? null : team.name.eq(teamname);
    }

    public BooleanExpression ageLoe(Integer age){
        return age != null ? member.age.loe(age) : null;
    }

    public BooleanExpression ageGoe(Integer age) {
        return age != null ? member.age.goe(age) : null;
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
       return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamname")
                ))
                .from(member)
                .where(
                        usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe())
                )
                .leftJoin(member.team, team)
                .fetch();

    }

    @Override
    public Page<MemberTeamDto> pagingComplicate(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> fetch = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamname")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = queryFactory
                .select(member)
                .from(member)
                .where(
                        usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe())
                )
                .fetchCount();
        return new PageImpl<>(fetch, pageable, count);
    }

    @Override
    public Page<MemberTeamDto> pagingSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamname")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> results1 = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(results1,pageable, total);
    }
}
