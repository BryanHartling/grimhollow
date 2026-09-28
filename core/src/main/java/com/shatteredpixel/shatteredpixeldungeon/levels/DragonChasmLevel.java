// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

/** A spanning maze with additional loops; platform timber never changes its collision geometry. */
public class DragonChasmLevel extends ExpeditionLevel {
    public static final int SIZE = 43, GRID = 7;
    public static final int[] EXIT_NODES = {0, 3, 6, 21, 27, 42, 45, 48};
    public int exitIndex;
    public int loopCount, deadEnds;
    { viewDistance = 6; }

    public static int nodeCell(int node) { return (3 + 6 * (node / GRID)) * SIZE + 3 + 6 * (node % GRID); }
    public static int centerCell() { return nodeCell(24); }

    @Override protected boolean build() {
        setSize(SIZE, SIZE);
        for (int y = 1; y < SIZE - 1; y++) for (int x = 1; x < SIZE - 1; x++) map[y * SIZE + x] = Terrain.CHASM;
        boolean[][] edges = new boolean[49][49];
        boolean[] seen = new boolean[49];
        ArrayList<Integer> stack = new ArrayList<>();
        stack.add(24); seen[24] = true;
        while (!stack.isEmpty()) {
            int node = stack.get(stack.size() - 1);
            ArrayList<Integer> next = neighbours(node);
            next.removeIf(n -> seen[n]);
            if (next.isEmpty()) { stack.remove(stack.size() - 1); continue; }
            int n = Random.element(next);
            edges[node][n] = edges[n][node] = true;
            seen[n] = true; stack.add(n);
        }
        ArrayList<int[]> shortcuts = new ArrayList<>();
        for (int n = 0; n < 49; n++) for (int m : neighbours(n))
            if (m > n && !edges[n][m]) shortcuts.add(new int[]{n, m});
        loopCount = 0;
        while (loopCount < 5 && !shortcuts.isEmpty()) {
            int[] edge = shortcuts.remove(Random.Int(shortcuts.size()));
            // Preserve at least four meaningful dead ends in the final graph.
            edges[edge[0]][edge[1]] = edges[edge[1]][edge[0]] = true;
            if (countDeadEnds(edges) < 4) edges[edge[0]][edge[1]] = edges[edge[1]][edge[0]] = false;
            else loopCount++;
        }
        deadEnds = countDeadEnds(edges);
        for (int n = 0; n < 49; n++) {
            int cell = nodeCell(n);
            for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) map[cell + y * SIZE + x] = Terrain.EMPTY_SP;
            for (int m : neighbours(n)) if (m > n && edges[n][m]) {
                int step = m - n == 1 ? 1 : SIZE;
                for (int c = cell; c != nodeCell(m); c += step) map[c] = Terrain.EMPTY_SP;
            }
        }
        exitIndex = Random.Int(8);
        int entrance = centerCell(), exit = nodeCell(EXIT_NODES[exitIndex]);
        map[entrance] = Terrain.ENTRANCE; map[exit] = Terrain.EXIT;
        transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE,
                DragonExpedition.CAVERN, DragonExpedition.BRANCH, LevelTransition.Type.REGULAR_EXIT));
        transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT,
                DragonExpedition.HOARD, DragonExpedition.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE));
        return loopCount >= 1 && deadEnds >= 4;
    }

    private static ArrayList<Integer> neighbours(int n) {
        ArrayList<Integer> result = new ArrayList<>();
        if (n % GRID > 0) result.add(n - 1);
        if (n % GRID < GRID - 1) result.add(n + 1);
        if (n >= GRID) result.add(n - GRID);
        if (n < 49 - GRID) result.add(n + GRID);
        return result;
    }
    private static int countDeadEnds(boolean[][] edges) {
        int result = 0;
        for (boolean[] row : edges) { int degree = 0; for (boolean e : row) if (e) degree++; if (degree == 1) result++; }
        return result;
    }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b); b.put("exit_index", exitIndex); b.put("loops", loopCount); b.put("dead_ends", deadEnds);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); exitIndex = b.getInt("exit_index"); loopCount = b.getInt("loops"); deadEnds = b.getInt("dead_ends");
    }
}
