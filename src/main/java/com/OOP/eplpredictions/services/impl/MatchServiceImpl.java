package com.OOP.eplpredictions.services.impl;

import com.OOP.eplpredictions.entities.Match;
import com.OOP.eplpredictions.entities.MatchEntity;
import com.OOP.eplpredictions.repositories.MatchApiRepository;
import com.OOP.eplpredictions.repositories.MatchRepository;
import com.OOP.eplpredictions.repositories.impl.FootballDataApiRepositoryImpl;
import com.OOP.eplpredictions.services.MatchService;
import com.OOP.eplpredictions.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final MatchApiRepository apiRepository;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, MatchApiRepository matchApiRepository) {
        this.matchRepository = matchRepository;
        this.apiRepository = matchApiRepository;
    }

    @Override
    public Match createMatch(Match match) {
        return matchEntityToMatch(matchRepository.save(matchToMatchEntity(match)));
    }

    @Override
    public Match updateMatch(Match match) {
        return matchEntityToMatch(matchRepository.save(matchToMatchEntity(match)));
    }

    @Override
    public void deleteMatch(int id) {
        matchRepository.deleteById(id);
    }

    @Override
    public Optional<Match> getMatchById(int matchId) {
//        MatchEntity match = matchRepository.findById(matchId);
        Optional<MatchEntity> match = matchRepository.findById(matchId);
        return match.map(this::matchEntityToMatch);
    }

    @Override
    public List<Match> getAllMatches() {
        List<MatchEntity> matchEntities = matchRepository.findAll();
        return matchEntities.stream().map(this::matchEntityToMatch).toList();
    }

    @Override
    public List<Match> getSchedule() {
        return getAllMatches().stream()
                .filter(match -> DateUtil.isScheduleDate(match.getTime())).toList();
//                .filter(match -> !DateUtil.isFirstDate7DaysLater(match.getTime(), new Date())
//                && new Date().before(match.getTime())).toList();
    }

    private Match matchEntityToMatch(MatchEntity matchEntity) {
        if (!Objects.equals(matchEntity.getStatus(), "incomplete")) {
            return Match.builder()
                    .id(matchEntity.getId())
                    .homeName(matchEntity.getHomeName())
                    .awayName(matchEntity.getAwayName())
                    .time(matchEntity.getTime())
                    .score(matchEntity.getStatus())
                    .status("complete")
                    .build();
        }

        if(new Date().before(matchEntity.getTime())){// checks if date is  later than today
            return Match.builder()
                    .id(matchEntity.getId())
                    .homeName(matchEntity.getHomeName())
                    .awayName(matchEntity.getAwayName())
                    .time(matchEntity.getTime())
                    .score("- : -")
                    .status("incomplete")
                    .build();
        }

        Match match = apiRepository.getMatch(matchEntity.getId());

        if (Objects.equals(match.getStatus(), "complete")) {
            matchRepository.save(matchToMatchEntity(match));
            System.out.println(matchEntity.getTime());
        }

        return match;
    }

    private MatchEntity matchToMatchEntity(Match match) {
        MatchEntity matchEntity = MatchEntity.builder()
                .id(match.getId())
                .homeName(match.getHomeName())
                .awayName(match.getAwayName())
                .time(match.getTime())
                .status(match.getStatus())
                .build();

        if (matchEntity.getStatus().equals("complete"))
            matchEntity.setStatus(match.getScore());

        return matchEntity;
    }
}
