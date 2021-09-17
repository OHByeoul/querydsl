package com.study.querydsl.repository;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void firstTest(){
        Member member = new Member("mem1", 10);
        memberRepository.save(member);

        Optional<Member> byId = memberRepository.findById(member.getId());
        Member member1 = byId.get();
        assertThat(member).isEqualTo(member1);
        assertThat(member1.getUsername()).isEqualTo("mem1");
        assertThat(member1.getAge()).isEqualTo(10);

        List<Member> all = memberRepository.findAll();
        assertThat(all).containsExactly(member);

        List<Member> mem1 = memberRepository.findByUsername("mem1");
        assertThat(mem1).containsExactly(member1);
    }

    @Test
    public void searchWhere(){
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);

        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //when
        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setUsername("member4");
        memberSearchCondition.setAgeGoe(32);
        memberSearchCondition.setAgeLoe(40);
        List<MemberTeamDto> memberTeamDtos = memberRepository.search(memberSearchCondition);

        //then
        assertThat(memberTeamDtos).extracting("username").containsExactly("member4");

    }

}