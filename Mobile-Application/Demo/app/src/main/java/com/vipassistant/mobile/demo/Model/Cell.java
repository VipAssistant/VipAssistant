package com.vipassistant.mobile.demo.Model;

import com.vipassistant.mobile.demo.Util.StringUtility;

public class Cell {

    public Player player;

    public Cell(Player player) {
        this.player = player;
    }

    public boolean isEmpty() {
        return player == null || StringUtility.isNullOrEmpty(player.value);
    }
}
