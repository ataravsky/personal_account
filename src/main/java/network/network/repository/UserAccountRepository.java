package network.network.repository;

import network.network.model.Role;
import network.network.model.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    UserAccount findByUsername(String username);


    @Query("select u from UserAccount u where (:username = '' or u.username = :username) and (:role MEMBER OF u.roles or :role = '')")
    Page<UserAccount> test(Role role, String username, Pageable pageable);

    @Query("select u from UserAccount u where (:username = '' or u.username = :username)")
    Page<UserAccount> test(String username, Pageable pageable);

    Page<UserAccount> findAll(Pageable pageable);



}
