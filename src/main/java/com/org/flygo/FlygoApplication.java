package com.org.flygo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
public class FlygoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlygoApplication.class, args);

    }


//    public List<String> invalidTransaction(String[] transactions) {
//        List<Transaction> parsedTransaction = new ArrayList<>();
//
//        for (int i = 0; i < transactions.length; i++) {
//            parsedTransaction.add(parse(transactions[i], i));
//        }
//
//        return invalidTransactions(parsedTransaction);
//    }
//
//    private List<String> invalidTransactions(List<Transaction> parsedTransaction) {
//        ///  add the list of transactions to the easily accessible group of transactions
//
//        Map<String, List<Transaction>> transactionGroup = new HashMap<>();
//        for (Transaction transaction : parsedTransaction) {
//            transactionGroup.computeIfAbsent(transaction.name, k -> new ArrayList<>());
//        }
//
//        boolean[] invalidTransaction = new boolean[transactionGroup.size()];
//        ///  Rule 1: amount > 60
//        for (Transaction transaction : parsedTransaction) {
//            if (transaction.amount.compareTo(BigDecimal.valueOf(1000)) > 0){
//                invalidTransaction[transaction.originalIndex] = true;
//            }
//        }
//
//        for (Map.Entry<String, List<Transaction>> entry : transactionGroup.entrySet()) {
//
//            List<Transaction> transactions = entry.getValue();
//
//            transactions.sort(Comparator.comparingInt(Transaction::time));
//
//
//            Map<String , Integer> tp = new ConcurrentHashMap<>();
//
//
//            int index = 0;
//            for (int left = 0; left < transactions.size(); left++) {
//                Transaction currentTrans = transactions.getFirst();
//
//                tp.put(currentTrans.city(), tp.getOrDefault(currentTrans.city, 0) + 1) ;
//
//
//                while(currentTrans.time() - transactions.get(index).time > 60){
//
//                    Transaction nowTrans = transactions.get(index);
//
//                    tp.put(nowTrans.city(), tp.getOrDefault(nowTrans.city, 0) + 1) ;
//
//                    if (tp.get(nowTrans.city())  == 0){
//                        tp.remove(nowTrans.city());
//                    }
//
//                    index++;
//                }
//
//                int windowSize = left - index +1 ;
//                int sameCityCount = tp.getOrDefault(currentTrans.city(), 0);
//
//                if (windowSize > sameCityCount){
//                    invalidTransaction[currentTrans.originalIndex()] = true;
//                }
//
//
//                tp.clear();
//
//                int rigtIndex = transactions.size() - 1;
//
//                for (int leftIndex = transactions.size() - 1; leftIndex >= 0; leftIndex--) {
//
//                    Transaction current = transactions.get(leftIndex);
//
//                    tp.put(current.city(), tp.getOrDefault(current
//                            .city(),0) + 1);
//                    while(current.time() - transactions.get(leftIndex).time() > 60){
//
//                        Transaction removed = transactions.get(leftIndex);
//
//                        tp.put(current.city(), tp.getOrDefault(current.city(), 0) - 1) ;
//
//                        if (tp.get(current.city()) == 0){
//                            tp.remove(removed.city());
//                        }
//                        leftIndex--;
//                    }
//
//
//                    int window = leftIndex - rigtIndex - 1;
//                }
//
//
//
//
//            }
//
//
//
//
//        }
//
//
//
//
//    }
//
//    private Transaction parse(String transactions, int index) {
//        String[] transaction = transactions.split(",");
//
//        String name = transaction[0];
//        BigDecimal amount = new BigDecimal(transaction[1]);
//        int time = Integer.parseInt(transaction[2]);
//        String city = transaction[3];
//
//        return new Transaction(
//                name,
//                amount,
//                time,
//                city,
//                index
//        );
//    }
//
//    record Transaction(String name , BigDecimal amount, int time,String city, int originalIndex) {}
}
