package com.jpmc.midascore.services;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class CustomerKafkaConsumer {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CustomerKafkaConsumer(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) throws Exception {
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord receiver = userRepository.findById(transaction.getRecipientId()).orElse(null);

        float amount = transaction.getAmount();

        // Check if both users exist and sender has enough funds
        if (sender != null && receiver != null && sender.getBalance() >= amount) {
            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);

            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setSender(sender);
            transactionRecord.setRecipient(receiver);
            transactionRecord.setAmount(amount);

            // Save all updates to the database
            userRepository.save(sender);
            userRepository.save(receiver);
            transactionRepository.save(transactionRecord);
        }


    }

}
