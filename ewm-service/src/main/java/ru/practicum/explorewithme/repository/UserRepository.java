package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select  u from User u where u.id in :ids")
    List<User> getUsersById(List<Long> ids, PageRequest pageRequest);

    @Query("select  u from User u where u.id in :ids")
    List<User> getUsersById(List<Long> ids);
}
