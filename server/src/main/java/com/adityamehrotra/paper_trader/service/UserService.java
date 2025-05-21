package com.adityamehrotra.paper_trader.service;

import com.adityamehrotra.paper_trader.dto.LoginRequest;
import com.adityamehrotra.paper_trader.dto.RegistrationRequest;
import com.adityamehrotra.paper_trader.model.User;
import com.adityamehrotra.paper_trader.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public UserService(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public int getNextID() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "userID"));
        query.limit(1);

        User lastUser = mongoTemplate.findOne(query, User.class);
        if (lastUser == null) {
            return 1;
        } else {
            return lastUser.getUserID() + 1;
        }
    }

    @Transactional
    public int registerUser(RegistrationRequest user) {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (user.getDateCreated() == null || user.getDateCreated().toString().isEmpty()) {
            throw new IllegalArgumentException("Date created cannot be empty");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        int userID = getNextID();

        List<Integer> portfolioList = new ArrayList<>();

        User newUser = new User(
                userID,
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getDateCreated(),
                portfolioList
        );

        userRepository.save(newUser);

        return userID;
    }

    @Transactional
    public int authenticateUser(LoginRequest user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return existingUser.getUserID();
    }

    @Transactional
    public User getUser(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid User ID");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return user;
    }

    @Transactional
    public void updateFirstName(Integer id, String firstName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setFirstName(firstName);
        userRepository.save(user);
    }

    @Transactional
    public void updateLastName(Integer id, String lastName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setLastName(lastName);
        userRepository.save(user);
    }

    @Transactional
    public void updateUsername(Integer id, String username) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setUsername(username);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Integer id, String password) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setPassword(password);
        userRepository.save(user);
    }

    @Transactional
    public void updateEmail(Integer id, String email) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findByUserID(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void addPortfolio(Integer userID, Integer portfolioID) {
        if (userID == null || userID <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        User user = userRepository.findByUserID(userID);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<Integer> portfolioList = user.getPortfolioList();
        if (!portfolioList.contains(portfolioID)) {
            portfolioList.add(portfolioID);
            user.setPortfolioList(portfolioList);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Portfolio ID already exists in the user's portfolio list");
        }
    }

    @Transactional
    public void removePortfolio(Integer userID, Integer portfolioID) {
        if (userID == null || userID <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (portfolioID == null || portfolioID <= 0) {
            throw new IllegalArgumentException("Invalid portfolio ID");
        }

        User user = userRepository.findByUserID(userID);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<Integer> portfolioList = user.getPortfolioList();
        if (portfolioList.contains(portfolioID)) {
            portfolioList.remove(portfolioID);
            user.setPortfolioList(portfolioList);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Portfolio ID does not exist in the user's portfolio list");
        }
    }

    @Transactional
    public List<Integer> getPortfolioList(Integer userID) {
        if (userID == null || userID <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findByUserID(userID);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return user.getPortfolioList();
    }

    @Transactional
    public void clearPortfolioList(Integer userID) {
        if (userID == null || userID <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findByUserID(userID);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setPortfolioList(new ArrayList<>());
        userRepository.save(user);
    }
}
