package ru.monsterdev.mosregtrader.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.monsterdev.mosregtrader.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
