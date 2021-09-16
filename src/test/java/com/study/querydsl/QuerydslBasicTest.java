package com.study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberDto;
import com.study.querydsl.dto.QMemberDto;
import com.study.querydsl.dto.UserDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import com.study.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.study.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",10,teamA);

        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",10,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){

        QMember m = new QMember("m");


        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        QMember member = new QMember("m");

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                    .and(member.age.between(10,30)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        QMember member = new QMember("m");

        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.between(10,30)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        queryFactory
                .selectFrom(member)
                .fetchResults();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        
        results.getTotal();
        List<Member> content = results.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member1 = result.get(0);
        Member member2 = result.get(0);
        Member memberNull = result.get(0);

        assertThat(member1.getUsername()).isEqualTo("member5");
        assertThat(member2.getUsername()).isEqualTo("member5");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2(){
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(40);

    }

    /**
     * 팀 이름과 각팀의 평균 연령은??
     */
    @Test
    public void group(){
        List<Tuple> result = queryFactory
                .select(QTeam.team.name, member.age.avg())
                .from(member)
                .join(member.team, QTeam.team)
                .groupBy(QTeam.team.name)
                .fetch();

        Tuple team1 = result.get(0);
        Tuple team2 = result.get(1);

        assertThat(team1.get(QTeam.team.name)).isEqualTo("teamA");
        assertThat(team1.get(member.age.avg())).isEqualTo(10);
    }

    /**
     * 팀 a에 소속된 모든 회원은?
     */
    @Test
    public void join(){
        List<Member> teamA = queryFactory
                .selectFrom(member)
                .join(member.team, QTeam.team)
                .where(QTeam.team.name.eq("teamA"))
                .fetch();

        assertThat(teamA.size()).isEqualTo(2);
        assertThat(teamA)
                .extracting("username")
                .contains("member1","member2");

    }

    /**
     * 세타조인
     */
    @Test
    public void thetaJoin(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, QTeam.team)
                .where(member.username.eq(QTeam.team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

    /*
    *  회원과 팀을 조인하는데 팀이 a인팀만 조인 회원은 모두 조회
     */
    @Test
    public void leftJoin(){
        List<Tuple> result = queryFactory
                .select(member, QTeam.team)
                .from(member)
                .leftJoin(member.team, QTeam.team)
                .on(QTeam.team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        assertThat(result.size()).isEqualTo(4);
    }

    /*
    * 연관관계 없는 엔티티 외부 조인
    * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */

    @Test
    public void join_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, QTeam.team)
                .from(member)
                .leftJoin(QTeam.team).on(member.username.eq(QTeam.team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void notFetchJoin(){
        em.flush();
        em.clear();

        Member member = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoin(){
        em.flush();
        em.clear();

        Member member = queryFactory
                .selectFrom(QMember.member)
                .join(QMember.member.team,QTeam.team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member.getTeam());
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    /*
    * 나이가 가장 많은 회원 조회
    * */
    @Test
    public void subQuery(){
        QMember memSub = new QMember("memSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memSub.age.max())
                                .from(memSub)

                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30);

    }

    /*
     * 나이가 가장 많은 회원 조회
     * */
    @Test
    public void subQueryGoe(){
        QMember memsub = new QMember("memsub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memsub.age.avg())
                                .from(memsub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30);
    }

    @Test
    public void subQueryIn(){
        QMember submem = new QMember("submem");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(submem.age)
                                .from(submem)
                                .where(submem.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30);
    }

    @Test
    public void selectSubq(){
        QMember submem = new QMember("submem");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(submem.age.avg())
                                .from(submem)
                ).from(submem)
                .fetch();

    }

    @Test
    public void basicCase(){
        List<String> fetch = queryFactory
                .select(member.age
                        .when(10).then("tttt")
                        .when(20).then("twen")
                        .otherwise("other")
                )
                .from(member)
                .fetch();

        for(String s : fetch){
            System.out.println("fetch = " + s);
        }
    }

    @Test
    public void complexCase(){
        List<String> fetch = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 30)).then("0~30333")
                        .when(member.age.between(31, 60)).then("31~60")
                        .otherwise("ow")
                )
                .from(member)
                .fetch();

        for(String s : fetch){
            System.out.println("fetch = " + s);
        }
    }

    @Test
    public void constant(){
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for(Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat(){
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for(String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void simpleProjection(){
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection(){
        List<Tuple> fetch = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
        }

    }

    @Test
    public void findDtoByJPQL(){
        List<MemberDto> resultList = em.createQuery("select new com.study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoBySetter(){
        List<MemberDto> fetch = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField(){
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        ExpressionUtils.as(member.username,"name"),
                        member.age
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : fetch) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtobyconstructor(){
        List<UserDto> name = queryFactory
                .select(Projections.constructor(UserDto.class,
                        ExpressionUtils.as(member.username, "name"),
                        member.age
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : name) {
            System.out.println("userDto = " + userDto);
        }

    }

    @Test
    public void findUserDtoByField(){
        QMember memSub = new QMember("memSub");

        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username,
                        ExpressionUtils.as(JPAExpressions.select(member.age.avg())
                                .from(memSub),"age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : fetch) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtoByProjection(){
        List<MemberDto> fetch = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("QMemberDto = " + memberDto);
        }
    }

    @Test
    public void booleanBuilderPrac(){
        String nameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchParam(nameParam,ageParam);
        assertThat(result.get(0).getAge()).isEqualTo(10);
    }

    private List<Member> searchParam(String nameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();

        if(nameParam != null){
            builder.and(member.username.eq(nameParam));
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        List<Member> fetch = queryFactory
                .select(member)
                .from(member)
                .where(builder)
                .fetch();
        return fetch;
    }

    @Test
    public void dynamicWhereParam(){
        String userParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMem2(userParam,ageParam);
        assertThat(result.get(0).getAge()).isEqualTo(10);
    }

    private List<Member> searchMem2(String userParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(userParam, ageParam))
                .fetch();
    }

    private BooleanBuilder usernameEq(String userParam) {
        if (userParam != null) {
            return new BooleanBuilder();
        }
        return new BooleanBuilder(member.username.eq(userParam));
    }

    private BooleanBuilder ageEq(Integer ageParam) {
        return ageParam != null ? new BooleanBuilder(member.age.eq(ageParam)) : new BooleanBuilder();
    }

    private BooleanBuilder allEq(String userParam, Integer ageParam){
        return usernameEq(userParam).and(ageEq(ageParam));
    }

    /*
    * 벌크연산
    * */
    @Test
    public void bulk(){

        long count = queryFactory
                .update(member)
                .set(member.username, "rrr")
                .where(member.age.lt(25))
                .execute();

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.lt(25))
                .fetch();

        for (Member m : fetch) {
            System.out.println("m = " + m);
        }
    }

    @Test
    public void bulkAdd(){
        long execute = queryFactory
                .update(member)
                .set(member.age, member.age.add(3))
                .where(member.age.lt(25))
                .execute();

        em.flush();
        em.clear();

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.lt(25))
                .fetch();

        assertThat(fetch.size()).isEqualTo(3);
    }

    @Test
    public void bulkDelete(){
        long execute = queryFactory
                .delete(member)
                .where(member.age.gt(25))
                .execute();

        em.flush();
        em.clear();

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.gt(25))
                .fetch();

        assertThat(fetch.size()).isEqualTo(0);

    }
}
