package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    Member member1;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() throws Exception {
        // member1
        Member findMember = em.createQuery(
                        "select m from Member m where m.username = :username", Member.class
                )
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(member1).isEqualTo(findMember);
        System.out.println("member1 == findMember >> " + (member1 == findMember)); // true
    }

    @Test
    public void startQueryDSL() throws Exception {
        //QMember m = new QMember("m");
        QMember m = member;
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();
        /*
        * select
            m1_0.member_id,
            m1_0.age,
            m1_0.team_id,
            m1_0.username
        from member m1_0
        where m1_0.username=?
        * */

        System.out.println("============ member1 == findMember >> " + (member1 == findMember)); // true
    }

    @Test
    public void search() throws Exception {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void searchAndParam() throws Exception {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), // 콤마로 구분할 경우 and로 연결된다
                        (member.age.eq(10)))
                .fetchOne();
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void resultFetch() throws Exception {
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        long total = results.getTotal();
        List<Member> content = results.getResults();
    }

    @Test
    public void sort() throws Exception {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.asc(), member.username.asc().nullsLast())
                .fetch();
    }
    
    @Test
    public void paging1() throws Exception {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
    }

    @Test
    public void aggregation() throws Exception {
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

        Long memberCount = tuple.get(member.count());
        Integer memberAge = tuple.get(member.age.sum());
        System.out.println(memberCount + ", " + memberAge);
    }
    
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
    }
}
