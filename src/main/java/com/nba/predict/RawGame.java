package com.nba.predict;

import com.opencsv.bean.CsvBindByName;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RawGame {
    @CsvBindByName(column = "game_date")
    private String gameDateStr;

    @CsvBindByName(column = "team_id_home")
    private String teamIdHome;

    @CsvBindByName(column = "team_id_away")
    private String teamIdAway;

    @CsvBindByName(column = "pts_home")
    private String ptsHomeStr;

    @CsvBindByName(column = "pts_away")
    private String ptsAwayStr;

    @CsvBindByName(column = "wl_home")
    private String wlHome;

    @CsvBindByName(column = "fg_pct_home")
    private String fgPctHomeStr;

    @CsvBindByName(column = "fg_pct_away")
    private String fgPctAwayStr;

    @CsvBindByName(column = "reb_home")
    private String rebHomeStr;

    @CsvBindByName(column = "reb_away")
    private String rebAwayStr;

    @CsvBindByName(column = "ast_home")
    private String astHomeStr;

    @CsvBindByName(column = "ast_away")
    private String astAwayStr;

    @CsvBindByName(column = "tov_home")
    private String tovHomeStr;

    @CsvBindByName(column = "tov_away")
    private String tovAwayStr;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LocalDate getGameDate() {
        if (gameDateStr == null || gameDateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(gameDateStr.split(" ")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public String getTeamIdHome() {
        return teamIdHome;
    }

    public String getTeamIdAway() {
        return teamIdAway;
    }

    public int getPtsHome() {
        try {
            return ptsHomeStr != null && !ptsHomeStr.isEmpty() ? (int) Double.parseDouble(ptsHomeStr) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getPtsAway() {
        try {
            return ptsAwayStr != null && !ptsAwayStr.isEmpty() ? (int) Double.parseDouble(ptsAwayStr) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isHomeWin() {
        return "W".equals(wlHome);
    }

    public String getWlHome() {
        return wlHome;
    }

    public double getFgPctHome() {
        try {
            return fgPctHomeStr != null && !fgPctHomeStr.isEmpty() ? Double.parseDouble(fgPctHomeStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getFgPctAway() {
        try {
            return fgPctAwayStr != null && !fgPctAwayStr.isEmpty() ? Double.parseDouble(fgPctAwayStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getRebHome() {
        try {
            return rebHomeStr != null && !rebHomeStr.isEmpty() ? Double.parseDouble(rebHomeStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getRebAway() {
        try {
            return rebAwayStr != null && !rebAwayStr.isEmpty() ? Double.parseDouble(rebAwayStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getAstHome() {
        try {
            return astHomeStr != null && !astHomeStr.isEmpty() ? Double.parseDouble(astHomeStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getAstAway() {
        try {
            return astAwayStr != null && !astAwayStr.isEmpty() ? Double.parseDouble(astAwayStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getTovHome() {
        try {
            return tovHomeStr != null && !tovHomeStr.isEmpty() ? Double.parseDouble(tovHomeStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getTovAway() {
        try {
            return tovAwayStr != null && !tovAwayStr.isEmpty() ? Double.parseDouble(tovAwayStr) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Setters for programmatic population (e.g., SQLite)
    public void setGameDateStr(String gameDateStr) {
        this.gameDateStr = gameDateStr;
    }

    public void setTeamIdHome(String teamIdHome) {
        this.teamIdHome = teamIdHome;
    }

    public void setTeamIdAway(String teamIdAway) {
        this.teamIdAway = teamIdAway;
    }

    public void setPtsHomeStr(String ptsHomeStr) {
        this.ptsHomeStr = ptsHomeStr;
    }

    public void setPtsAwayStr(String ptsAwayStr) {
        this.ptsAwayStr = ptsAwayStr;
    }

    public void setWlHome(String wlHome) {
        this.wlHome = wlHome;
    }

    public void setFgPctHomeStr(String fgPctHomeStr) {
        this.fgPctHomeStr = fgPctHomeStr;
    }

    public void setFgPctAwayStr(String fgPctAwayStr) {
        this.fgPctAwayStr = fgPctAwayStr;
    }

    public void setRebHomeStr(String rebHomeStr) {
        this.rebHomeStr = rebHomeStr;
    }

    public void setRebAwayStr(String rebAwayStr) {
        this.rebAwayStr = rebAwayStr;
    }

    public void setAstHomeStr(String astHomeStr) {
        this.astHomeStr = astHomeStr;
    }

    public void setAstAwayStr(String astAwayStr) {
        this.astAwayStr = astAwayStr;
    }

    public void setTovHomeStr(String tovHomeStr) {
        this.tovHomeStr = tovHomeStr;
    }

    public void setTovAwayStr(String tovAwayStr) {
        this.tovAwayStr = tovAwayStr;
    }
}

