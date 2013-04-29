package uk.ac.uea.cmp.phygen.core.ds.split;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Dan
 * Date: 28/04/13
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public class Weights {

    private final static Logger log = LoggerFactory.getLogger(Weights.class);

    private double[][] weights;

    public Weights(int nbTaxa) {
        this.weights = new double[nbTaxa][nbTaxa];
    }

    public Weights(double[][] weights) {

        if (weights == null) {
            this.weights = null;
        }

        int n = weights.length;

        if (n != weights[0].length)
            throw new IllegalArgumentException("Tree weights must be a square matrix");

        this.weights = new double[n][n];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                this.weights[i][j] = weights[i][j];
            }
        }
    }

    public double getAt(int i, int j) {
        return this.weights[i][j];
    }

    public void setValAt(double val, int i, int j) {
        this.weights[i][j] = val;
    }

    public int size() {
        return this.weights.length;
    }
}
