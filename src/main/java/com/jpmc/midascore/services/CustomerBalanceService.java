package com.jpmc.midascore.services;

import com.jpmc.midascore.component.BalanceController;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerBalanceService {
    private final UserRepository userRepository;

    public CustomerBalanceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Balance getBalanceForCustomer(long id){
        Balance balance= new Balance();
        UserRecord userRecord = userRepository.findById(id).orElse(new UserRecord("ntg",0));
        balance.setAmount(userRecord.getBalance());
        return balance;
    }
}
