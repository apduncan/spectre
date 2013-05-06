/*
 * Phylogenetics Tool suite
 * Copyright (C) 2013  UEA CMP Phylogenetics Group
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.uea.cmp.phygen.superq.optimise.gurobi;

import gurobi.*;
import org.apache.log4j.Logger;
import uk.ac.uea.cmp.phygen.superq.optimise.Optimiser;
import uk.ac.uea.cmp.phygen.superq.optimise.OptimiserException;
import uk.ac.uea.cmp.phygen.superq.optimise.Problem;


public abstract class GurobiOptimiser implements Optimiser {
    
    static Logger logger = Logger.getLogger(GurobiOptimiser.class);
    
    // Input vars
    private Problem problem;
    private int length;
    
    // GurobiOptimiser vars
    private GRBEnv env;            
    private GRBModel model;
    private GRBVar[] variables;
    
    public GurobiOptimiser() throws OptimiserException {
        try {    
            //GRBEnv env = new GRBEnv("gurobi.log");
            this.env = new GRBEnv();
            this.env.set(GRB.IntParam.OutputFlag, 0);
            this.model = new GRBModel(env);
        } catch (GRBException ge) {
            // Repackage any GurobiException and rethrow
            throw new OptimiserException(ge, ge.getErrorCode());
        }
    }
    
    public abstract void setVariables() throws GRBException;
    public abstract void addConstraints() throws GRBException;
    public abstract GRBExpr getObjective() throws GRBException;
    
    protected void initVariables(Problem problem) {
        this.length = problem.getRestriction().length;
        this.variables = new GRBVar[this.length];        
        this.problem = problem; 
    }
    
    @Override
    public double[] optimise(Problem problem) throws OptimiserException {
        
        double[] solution = null;
        
        try {
            // Initalise variables
            initVariables(problem);

            // Set the variables according to sub class
            setVariables();   

            // Update the model
            model.update();

            // Get the objective if present
            GRBExpr expr = getObjective();

            if (expr != null) {
                model.setObjective(expr);
            }

            // Add constraints
            addConstraints();

            // Optimise the model
            model.optimize();

            // Create solution array from the optimised model
            solution = this.buildSolution();

            // Logging
            logger.debug("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
        
        } catch (GRBException ge) {
            // Repackage any GurobiException and rethrow
            throw new OptimiserException(ge, ge.getErrorCode());
        }
        
        return solution;
    }

    public int getLength() {
        return length;
    }
    
    public double[] getRestriction() {
        return this.problem.getRestriction();
    }
    
    public double getRestrictionAt(final int i) {
        return this.getRestriction()[i];
    }

    public double[][] getMatrix() {
        return this.problem.getMatrix();
    }
    
    public double getMatrixAt(final int i, final int j) {
        return this.getMatrix()[i][j];
    }
    
    public int getMatrixRows() {
        return this.getMatrix().length;
    }
    
    public int getMatrixColumns() {
        if (this.getMatrixRows() <= 0)
            return 0;        
        return this.getMatrix()[0].length;
    }
    
    public double[] getCoefficients() {
        return this.problem.getCoefficients();
    }
    
    public double getCoefficientAt(final int i) {
        return this.getCoefficients()[i];
    }
    
    protected GRBEnv getEnv() {
        return env;
    }

    protected GRBModel getModel() {
        return model;
    }

    protected GRBVar[] getVariables() {
        return variables;
    }
    
    protected GRBVar getVariableAt(final int i) {
        return this.variables[i];
    }
    
    protected void setVariableAt(final int i, final GRBVar var) {
        this.variables[i] = var;
    }
    
    protected double[] buildSolution() throws GRBException {
        
        double[] solution = new double[this.getLength()];
        
        for (int i = 0; i < this.getLength(); i++) {
            solution[i] = this.getVariables()[i].get(GRB.DoubleAttr.X);
        }
        
        return solution;
    }
}
