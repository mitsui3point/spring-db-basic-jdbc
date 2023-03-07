package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepository {
    Member save(Member saveMember);
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
