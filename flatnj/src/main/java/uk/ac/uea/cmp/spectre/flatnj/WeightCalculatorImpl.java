/*
 * Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 * Copyright (C) 2017  UEA School of Computing Sciences
 *
 * This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.flatnj;

import uk.ac.earlham.metaopt.*;
import uk.ac.uea.cmp.spectre.core.ds.quad.quadruple.QuadrupleSystem;
import uk.ac.uea.cmp.spectre.core.ds.split.flat.PermutationSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes split weights in the resulting {@linkplain  PermutationSequence}.  Originally designed to use
 * <a href="http://www.gurobi.com/">Gurobi solver</a>, although now may use any solver supported by metaopt
 * <a href="https://github.com/maplesond/metaopt">MetaOpt</a>.
 *
 * @author balvociute
 */

public class WeightCalculatorImpl implements WeightCalculator {

    private PermutationSequence ps;
    private QuadrupleSystem qs;

    /**
     * Constructor used to initiate {@linkplain  WeightCalculator} and set
     * {@linkplain  PermutationSequence} and {@linkplain  QuadrupleSystem}.
     *
     * @param ps {@link PermutationSequence}.
     * @param qs {@link QuadrupleSystem}.
     */
    public WeightCalculatorImpl(PermutationSequence ps, QuadrupleSystem qs) {
        this.ps = ps;
        this.qs = qs;
    }


    protected List<Variable> createVariables(final int size) {

        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            variables.add(new Variable(
                    "x" + i,                                 // Name
                    new Bounds(0.0, Bounds.BoundType.LOWER), // Bounds
                    Variable.VariableType.CONTINUOUS         // Type
            ));
        }

        return variables;
    }

    /**
     * Ensures all variables are non-negative (are these actually required?  Might want to try removing these and see
     * if we get the same result, as we have already requested that the variables are non-negative.  Probably these
     * constraints will just slow the solver down.)
     *
     * @param variables Variables to constrain
     * @return A list of constraints
     */
    protected List<Constraint> createConstraints(List<Variable> variables) {

        List<Constraint> constraints = new ArrayList<>(variables.size());

        for (Variable var : variables) {

            Expression expr = new Expression().addTerm(1.0, var);

            constraints.add(new Constraint("c0", expr, Constraint.Relation.GREATER_THAN_OR_EQUAL_TO, 0.0));
        }

        return constraints;
    }

    protected Objective createObjective(List<Variable> variables) {

        // Extract linear and quadratic terms from ps and qs

        double[] linearCoefficients = ps.computebVector(qs);

        int[][] B = ps.computeBMatrix();

        double[][] quadraticCoefficients = new double[B.length][B[0].length];

        for (int i = 0; i < B.length; i++) {
            for (int j = 0; j < B[i].length; j++) {
                quadraticCoefficients[i][j] = B[i][j];
            }
        }

        double constant = qs.computeWxWT();

        // Create the phygen quadratic objective
        Expression quadExpr = new Expression();

        quadExpr.addConstant(constant);

        // Add linear terms
        for (int i = 0; i < variables.size(); i++) {
            quadExpr.addTerm(-2 * linearCoefficients[i], variables.get(i));
        }

        // Add quadratic terms
        for (int i = 0; i < variables.size(); i++) {
            for (int j = 0; j < variables.size(); j++) {
                quadExpr.addTerm(quadraticCoefficients[j][i], variables.get(j), variables.get(i));
            }
        }

        return new Objective("flatnj", Objective.ObjectiveDirection.MINIMISE, quadExpr);
    }

    @Override
    public void fitWeights(Optimiser optimiser) throws OptimiserException {
        //qs.normalizeWeights();

        List<Variable> variables = this.createVariables(ps.getnSwaps());
        List<Constraint> constraints = this.createConstraints(variables);
        Objective objective = this.createObjective(variables);


        Solution solution = optimiser.optimise(new Problem("flatnj", variables, constraints, objective));

        double[] weights = solution.getVariableValues();

        // ps.setFit();???
        ps.setWeights(weights);
        ps.setTrivial(qs.getTrivial());

    }

}
