package com.study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Hello;
import com.study.querydsl.entity.QHello;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@PersistenceContext
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");

		Hello result = queryFactory
				.selectFrom(qHello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());

	}

}
