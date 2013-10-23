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
package uk.ac.uea.cmp.phygen.core.math.optimise;


import java.util.ArrayList;
import java.util.List;

public class Problem {

    private String name;
    private List<Variable> variables;
    private List<Constraint> constraints;
    private Objective objective;
    private int maxIterations;
    private double tolerance;
    private boolean initialPoint;

    public Problem() {
        this("null", new ArrayList<Variable>(), new ArrayList<Constraint>(), null);
    }

    public Problem(String name, List<Variable> variables, List<Constraint> constraints, Objective objective) {
        this.name = name;
        this.variables = variables;
        this.constraints = constraints;
        this.objective = objective;
        this.maxIterations = 0;
        this.tolerance = 0.0;
        this.initialPoint = false;
    }

    public String getName() {
        return name;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public Objective getObjective() {
        return objective;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isInitialPointSet() {
        return initialPoint;
    }

    public void setInitialPoint(boolean initialPoint) {
        this.initialPoint = initialPoint;
    }

    public int getNbVariables() {
        if (this.variables == null)
            return 0;

        return this.variables.size();
    }

    public double[] getInitialPointCoefficients() {
        double[] coefficients = new double[variables.size()];

        for (int i = 0; i < variables.size(); i++) {
            coefficients[i] = variables.get(i).getCoefficient();
        }

        return coefficients;
    }

    public Objective.ObjectiveType getObjectiveType() {
        return this.objective.getType();
    }

    public Objective.ObjectiveDirection getObjectiveDirection() {
        return this.objective.getDirection();
    }

    public Constraint.ConstraintType getConstraintType() {

        for(Constraint constraint : this.constraints) {
            if (constraint.getType() == Constraint.ConstraintType.QUADRATIC) {
                return Constraint.ConstraintType.QUADRATIC;
            }
        }

        return Constraint.ConstraintType.LINEAR;
    }
}
