package com.testbot;

import org.springframework.data.repository.CrudRepository;
import org.telegram.telegrambots.meta.api.objects.User;

public interface UserRepository extends CrudRepository<User, String> {
}
