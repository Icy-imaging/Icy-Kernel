/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.math;

import java.util.Arrays;

/**
 * Implementation of the Hungarian / Munkres-Kuhn algorithm<br>
 * for rectangular assignment problem.
 * 
 * @author Nicolas Chenouard & Stephane
 */
public class HungarianAlgorithm
{
    final int numRow;
    final int numCol;
    final int k;
    final double[][] costs;

    final int[] rowsStar;
    final int[] colsStar;
    final int[] rowsPrime;
    final boolean[] rowsCovered;
    final boolean[] colsCovered;

    final int[] colsUnStar;
    final int[] rowsDoStar;

    int step;
    boolean done;

    /**
     * Create the optimizer.
     * 
     * @param values
     *        Table of assignment costs.<br>
     */
    public HungarianAlgorithm(double[][] values)
    {
        int r, c;
        final int initialNumCol = values[0].length;

        numRow = values.length;
        // number of column should >= number of row
        numCol = Math.max(initialNumCol, numRow);
        k = Math.min(numRow, numCol);
        costs = new double[numRow][numCol];

        // find maximum value
        double max = values[0][0];
        if (initialNumCol < numRow)
        {
            for (r = 0; r < values.length; r++)
            {
                final double v = ArrayMath.max(values[r]);
                if (v > max)
                    max = v;
            }
        }

        // enlarge matrix with max value if necessary
        for (r = 0; r < values.length; r++)
        {
            final double[] rowValues = values[r];
            final double[] rowCosts = costs[r];

            for (c = 0; c < rowValues.length; c++)
                rowCosts[c] = rowValues[c];
            for (; c < numCol; c++)
                rowCosts[c] = max;
        }

        rowsStar = new int[numRow];
        colsStar = new int[numCol];
        rowsPrime = new int[numRow];
        rowsCovered = new boolean[numRow];
        colsCovered = new boolean[numCol];

        colsUnStar = new int[numCol];
        rowsDoStar = new int[numRow];

        Arrays.fill(rowsPrime, -1);
    }

    /**
     * Resolve and returns result in this form : <code>result[row] = column</code>
     */
    public int[] resolve()
    {
        initialReduce();

        done = false;
        step = 2;
        while (!done)
        {
            switch (step)
            {
                case 2:
                    updateStar();
                    break;

                case 3:
                    doColCover();
                    break;

                case 4:
                    doPrime();
                    break;

                case 5:
                    // done inner 4
                    break;

                case 6:
                    reduce();
                    break;
            }
        }

        return rowsStar;
    }

    // For each row we find the row minimum and subtract it from all entries on that row.
    private void initialReduce()
    {
        for (int r = 0; r < numRow; r++)
        {
            final double[] rowCosts = costs[r];

            // get row minimum cost
            final double min = ArrayMath.min(rowCosts);

            // subtract it to all entries
            for (int c = 0; c < numCol; c++)
                rowCosts[c] -= min;
        }
    }

    // update starring
    private void updateStar()
    {
        Arrays.fill(rowsStar, -1);
        Arrays.fill(colsStar, -1);

        for (int r = 0; r < numRow; r++)
            updateRowStar(r);

        step = 3;
    }

    private void updateRowStar(int r)
    {
        final double[] rowCosts = costs[r];

        for (int c = 0; c < numCol; c++)
        {
            if (colsStar[c] == -1)
            {
                if (rowCosts[c] == 0)
                {
                    rowsStar[r] = c;
                    colsStar[c] = r;
                    return;
                }
            }
        }
    }

    // cover column with contained star
    private void doColCover()
    {
        Arrays.fill(colsCovered, false);

        int numColCovered = 0;
        for (int c = 0; c < numCol; c++)
        {
            if (colsStar[c] != -1)
            {
                colsCovered[c] = true;
                numColCovered++;
            }
        }

        if (numColCovered == k)
            done = true;
        else
            step = 4;
    }

    // prime uncovered zero
    private void doPrime()
    {
        for (int c = 0; c < numCol; c++)
            if (!colsCovered[c])
                if (doPrimCol(c))
                    return;

        step = 6;
    }

    // prime specified column
    private boolean doPrimCol(int c)
    {
        for (int r = 0; r < numRow; r++)
        {
            if (!rowsCovered[r])
            {
                // no covered zero ?
                if (costs[r][c] == 0)
                {
                    // prime it
                    rowsPrime[r] = c;

                    // get star column for this row ?
                    final int starCol = rowsStar[r];

                    // no star on this row
                    if (starCol == -1)
                    {
                        convertPrimeToStar(r, c);
                        return true;
                    }

                    rowsCovered[r] = true;
                    colsCovered[starCol] = false;

                    // so we don't forget newly uncovered zeros
                    if (starCol < c)
                        if (doPrimCol(starCol))
                            return true;
                }
            }
        }

        return false;
    }

    // convert all prime found on the way to star
    private void convertPrimeToStar(int r, int c)
    {
        int nb = 0;

        int primeCol = c;
        int starRow = colsStar[primeCol];

        while (starRow != -1)
        {
            colsUnStar[nb] = primeCol;
            rowsDoStar[nb] = starRow;
            nb++;

            primeCol = rowsPrime[starRow];
            starRow = colsStar[primeCol];
        }

        for (int i = 0; i < nb; i++)
        {
            final int startCol = colsUnStar[i];

            // unstar
            rowsStar[colsStar[startCol]] = -1;
            colsStar[startCol] = -1;
        }

        for (int i = 0; i < nb; i++)
        {
            final int primeRow = rowsDoStar[i];
            final int pc = rowsPrime[primeRow];

            // star
            colsStar[pc] = primeRow;
            rowsStar[primeRow] = pc;
        }
        // star
        colsStar[c] = r;
        rowsStar[r] = c;

        Arrays.fill(rowsPrime, -1);
        Arrays.fill(rowsCovered, false);
        Arrays.fill(colsCovered, false);

        step = 3;
    }

    // reduce costs
    private void reduce()
    {
        double min = Double.MAX_VALUE;

        // find minimum of uncovered elements
        for (int r = 0; r < numRow; r++)
        {
            if (!rowsCovered[r])
            {
                final double[] rowCosts = costs[r];

                for (int c = 0; c < numCol; c++)
                {
                    if (!colsCovered[c])
                    {
                        final double v = rowCosts[c];

                        if (v < min)
                            min = v;
                    }
                }
            }
        }

        // subtract minimum from uncovered elements
        // and add it to double covered elements
        for (int r = 0; r < numRow; r++)
        {
            final double[] rowCosts = costs[r];

            if (rowsCovered[r])
            {
                for (int c = 0; c < numCol; c++)
                    if (colsCovered[c])
                        rowCosts[c] += min;
            }
            else
            {
                for (int c = 0; c < numCol; c++)
                    if (!colsCovered[c])
                        rowCosts[c] -= min;
            }
        }

        step = 4;
    }
}
