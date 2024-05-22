package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    public void testEntity() throws Exception {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m",
                Member.class).getResultList();

        System.out.println("members.get(0).getTeam() == members.get(1).getTeam() =>> " + (members.get(0).getTeam() == members.get(1).getTeam()));
        // true

        for (Member member : members) {
            System.out.println("member => " + member);
            System.out.println("member.team => " + member.getTeam());
        }

        Assertions.assertThat(members.get(0).getTeam()).isEqualTo(members.get(1).getTeam());
        Assertions.assertThat(members.get(2).getTeam()).isEqualTo(members.get(3).getTeam());

        System.out.println("members.get(0).getTeam() == members.get(1).getTeam() =>> " + (members.get(0).getTeam() == members.get(1).getTeam()));
        // true
    }
}