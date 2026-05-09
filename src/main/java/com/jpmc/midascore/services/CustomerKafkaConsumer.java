package com.jpmc.midascore.services;

import com.jpmc.midascore.entity.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.logging.Logger;


@Service
public class CustomerKafkaConsumer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CustomerKafkaConsumer.class);
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public CustomerKafkaConsumer(UserRepository userRepository, TransactionRepository transactionRepository, RestTemplateBuilder builder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = builder
                .rootUri("http://localhost:8080")
                .build();
    }

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) throws Exception {
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord receiver = userRepository.findById(transaction.getRecipientId()).orElse(null);
        float amount = transaction.getAmount();
        Incentive incentive = restTemplate.postForObject("/incentive",transaction, Incentive.class);
        // Check if both users exist and sender has enough funds
        if (sender != null && receiver != null && sender.getBalance() >= amount) {
            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount + incentive.getAmount());

            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setSender(sender);
            transactionRecord.setRecipient(receiver);
            transactionRecord.setAmount(amount);
            transactionRecord.setIncentive(incentive.getAmount());

            // Save all updates to the database
            userRepository.save(sender);
            userRepository.save(receiver);
            transactionRepository.save(transactionRecord);
        }


    }

}
