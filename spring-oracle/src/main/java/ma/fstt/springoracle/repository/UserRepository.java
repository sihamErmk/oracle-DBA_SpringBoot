package ma.fstt.springoracle.repository;

import ma.fstt.springoracle.model.OracleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<OracleUser, Long> {
    Optional<OracleUser> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE OracleUser u SET u.lastLoginDate = ?2 WHERE u.username = ?1")
    void updateLastLoginDate(String username, LocalDateTime loginDate);

    @Modifying
    @Query("UPDATE OracleUser u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.username = ?1")
    void incrementFailedLoginAttempts(String username);

    @Modifying
    @Query("UPDATE OracleUser u SET u.accountLocked = ?2 WHERE u.username = ?1")
    void updateAccountLockStatus(String username, boolean locked);

    @Query(value = "SELECT profile FROM dba_users WHERE username = UPPER(?1)", nativeQuery = true)
    String getUserProfile(String username);

    @Query(value = "SELECT account_status FROM dba_users WHERE username = UPPER(?1)", nativeQuery = true)
    String getAccountStatus(String username);

}