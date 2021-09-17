package com.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.dto.QMemberTeamDto;
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
}
