package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.autogen.ModelParameterAT;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.netixx.AutoTopo.notifications.goals.SpeedGoal;

public class AutoTopoModel extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AutoTopoModel.class);

    private final IModelParameterAT param;

    AutoTopoModel(IModelParameterAT parameters) {
        super(ModelName.AT);
        this.param = parameters;
    }

    /**
     * Constructor.
     * 
     * @param v0
     *            desired velocity, m/s
     * @param a
     *            maximum acceleration, m/s^2
     * @param b
     *            desired deceleration (comfortable braking), m/s^2
     * @param T
     *            safe time headway, seconds
     * @param s0
     *            bumper to bumper vehicle distance in stationary traffic, meters
     * @param s1
     *            gap parameter, meters
     */
    public AutoTopoModel(double v0, double a, double b, double T, double s0, double s1) {
        super(ModelName.AT);
        this.param = create(v0, a, b, T, s0, s1);
    }

    private static ModelParameterAT create(double v0, double a, double b, double T, double s0, double s1) {
        ModelParameterAT modelParameterAT = new ModelParameterAT();
        modelParameterAT.setV0(v0);
        modelParameterAT.setA(a);
        modelParameterAT.setB(b);
        modelParameterAT.setT(T);
        modelParameterAT.setS0(s0);
        modelParameterAT.setS1(s1);
        modelParameterAT.setDelta(modelParameterAT.getDelta());
        return modelParameterAT;
    }


    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param TLocal
     *            the t local
     * @param v0Local
     *            the v0 local
     * @param aLocal
     *            the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0.0) {
            return 0.0;
        }

        final double s0 = getMinimumGap();
        double sstar = s0 + TLocal * v + param.getS1() * Math.sqrt((v + 0.0001) / v0Local) + (0.5 * v * dv)
                / Math.sqrt(aLocal * param.getB());

        if (sstar < s0) {
            sstar = s0;
        }

        final double aWanted = aLocal * (1.0 - Math.pow((v / v0Local), param.getDelta()) - (sstar / s) * (sstar / s));

        LOG.debug("aWanted = {}", aWanted);
        return aWanted; // limit to -bMax in Vehicle
    }


    @Override
    protected IModelParameterAT getParameter() {
        return param;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // SpeedGoal speedGoal = AutoTopoLink.getInstance().getSpeedGoal(me);
        SpeedGoal speedGoal = me.getAutoTopoSpeedGoal();
        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double aLead = frontVehicle == null ? me.getAcc() : frontVehicle.getAcc();

        // space dependencies modeled by speedlimits, alpha's

        final double Tlocal = alphaT * param.getT();
        // if(alphaT!=1){
        // System.out.printf("calcAcc: pos=%.2f, speed=%.2f, alphaT=%.3f, alphaV0=%.3f, T=%.3f, Tlocal=%.3f \n",
        // me.getPosition(), me.getSpeed(), alphaT, alphaV0, T, Tlocal);
        // }
        // consider external speedlimit
        final double v0Local = speedGoal != null ? Math.min(me.getSpeedlimit(), speedGoal.getSpeed()) : Math.min(
                alphaV0 * getDesiredSpeed(), me.getSpeedlimit());
        // if (speedGoal != null) {
        // v0Local = Math.min(me.getSpeedlimit(), speedGoal.getSpeed());
        // }
        final double aLocal = alphaA * param.getA();

        return acc(s, v, dv, aLead, Tlocal, v0Local, aLocal);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 0, param.getT(), getDesiredSpeed(), param.getA());
    }

    // Implementation of ACC model with improved IDM (IIDM)
    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param aLead
     *            the a lead
     * @param TLocal
     *            the t local
     * @param v0Local
     *            the v0 local
     * @param aLocal
     *            the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double aLead, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0) {
            return 0;
        }

        final double sstar = getMinimumGap()
                + Math.max(
                        TLocal * v + param.getS1() * Math.sqrt((v + 0.00001) / v0Local) + 0.5 * v * dv
                                / Math.sqrt(aLocal * param.getB()), 0.);
        final double z = sstar / Math.max(s, 0.01);
        final double accEmpty = (v <= v0Local) ? aLocal * (1 - Math.pow((v / v0Local), param.getDelta())) : -param
                .getB() * (1 - Math.pow((v0Local / v), aLocal * param.getDelta() / param.getB()));
        final double accPos = accEmpty * (1. - Math.pow(z, Math.min(2 * aLocal / accEmpty, 100.)));
        final double accInt = aLocal * (1 - z * z);

        final double accIIDM = (v < v0Local) ? (z < 1) ? accPos : accInt : (z < 1) ? accEmpty : accInt + accEmpty;

        // constant-acceleration heuristic (CAH)

        final double aLeadRestricted = Math.min(aLead, aLocal);
        final double dvp = Math.max(dv, 0.0);
        final double vLead = v - dvp;
        final double denomCAH = vLead * vLead - 2 * s * aLeadRestricted;

        final double accCAH = ((vLead * dvp < -2 * s * aLeadRestricted) && (denomCAH != 0)) ? v * v * aLeadRestricted
                / denomCAH : aLeadRestricted - 0.5 * dvp * dvp / Math.max(s, 0.0001);

        // ACC with IIDM

        final double accACC_IIDM = (accIIDM > accCAH) ? accIIDM : (1 - param.getCoolness()) * accIIDM
                + param.getCoolness() * (accCAH + param.getB() * Math.tanh((accIIDM - accCAH) / param.getB()));

        return accACC_IIDM;
    }

}
