package com.example.demo.controller;

import com.example.demo.entity.Competition;
import com.example.demo.repository.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private CompetitionRepository competitionRepository;

    @GetMapping("/competition/{id}")
    public Competition getCompetition(@PathVariable Long id) {
        return competitionRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/public/competition/{id}")
    public Competition getCompetitionPublic(@PathVariable Long id) {
        return competitionRepository.findById(id).orElse(null);
    }
}