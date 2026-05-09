package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.services.CustomerBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class BalanceController {
    private final CustomerBalanceService customerBalanceService;

    public BalanceController(CustomerBalanceService customerBalanceService) {
        this.customerBalanceService = customerBalanceService;
    }

    @GetMapping("/balance")
    public Balance balance(@RequestParam long userId){
        return customerBalanceService.getBalanceForCustomer(userId);
    }
}
