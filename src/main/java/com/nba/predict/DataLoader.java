package com.nba.predict;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataLoader {
    
    public List<RawGame> loadGames(String filePath) throws Exception {
        if (filePath.endsWith(".sqlite") || filePath.endsWith(".db")) {
            return loadFromSqlite(filePath);
        }
        return loadFromCsv(filePath);
    }

    private List<RawGame> loadFromCsv(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            CsvToBean<RawGame> csvToBean = new CsvToBeanBuilder<RawGame>(reader)
                    .withType(RawGame.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            
            List<RawGame> games = csvToBean.parse();
            
            // Filter out games with invalid dates and sort chronologically
            return games.stream()
                    .filter(game -> game.getGameDate() != null)
                    .sorted(Comparator.comparing(RawGame::getGameDate))
                    .collect(Collectors.toList());
        }
    }

    private List<RawGame> loadFromSqlite(String filePath) throws Exception {
        List<RawGame> games = new ArrayList<>();
        String url = "jdbc:sqlite:" + filePath;
        String sql = "SELECT game_date, team_id_home, team_id_away, pts_home, pts_away, wl_home, " +
                "fg_pct_home, fg_pct_away, reb_home, reb_away, ast_home, ast_away, tov_home, tov_away " +
                "FROM game ORDER BY game_date ASC";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RawGame g = new RawGame();
                g.setGameDateStr(rs.getString("game_date"));
                g.setTeamIdHome(rs.getString("team_id_home"));
                g.setTeamIdAway(rs.getString("team_id_away"));
                g.setPtsHomeStr(String.valueOf(rs.getDouble("pts_home")));
                g.setPtsAwayStr(String.valueOf(rs.getDouble("pts_away")));
                g.setWlHome(rs.getString("wl_home"));
                g.setFgPctHomeStr(String.valueOf(rs.getDouble("fg_pct_home")));
                g.setFgPctAwayStr(String.valueOf(rs.getDouble("fg_pct_away")));
                g.setRebHomeStr(String.valueOf(rs.getDouble("reb_home")));
                g.setRebAwayStr(String.valueOf(rs.getDouble("reb_away")));
                g.setAstHomeStr(String.valueOf(rs.getDouble("ast_home")));
                g.setAstAwayStr(String.valueOf(rs.getDouble("ast_away")));
                g.setTovHomeStr(String.valueOf(rs.getDouble("tov_home")));
                g.setTovAwayStr(String.valueOf(rs.getDouble("tov_away")));
                games.add(g);
            }
        }

        return games.stream()
                .filter(game -> game.getGameDate() != null)
                .sorted(Comparator.comparing(RawGame::getGameDate))
                .collect(Collectors.toList());
    }
}

