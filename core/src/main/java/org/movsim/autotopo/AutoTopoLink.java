package org.movsim.autotopo;

import java.io.IOException;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.vehicles.Vehicle;

import fr.netixx.AutoTopo.adapters.IVehicleAdapter;
import fr.netixx.AutoTopo.adapters.impl.movsim.AutoTopoMovsimController;
import fr.netixx.AutoTopo.notifications.goals.LaneChangeGoal;

public class AutoTopoLink implements SimulationTimeStep {

    private final double precision = 0.00001;
    private final double interval = 1;

    private final int autoTopoStart = 0;

    private AutoTopoMovsimController controller;

    private AutoTopoLink() {
        controller = new AutoTopoMovsimController();
    }

    private static AutoTopoLink instance;

    public static AutoTopoLink getInstance() {
        if (instance == null) {
            instance = new AutoTopoLink();
        }
        return instance;
    }

    public void addVehicle(Vehicle vh) {
        controller.addVehicle(vh);
    }

    public void removeVehicle(Vehicle vh) {
        controller.removeVehicle(vh);
    }

    public static void reset() {
        instance = null;
    }

    private static SimulationRunnable simulationRunnable;

    public static void setSimulationRunnable(SimulationRunnable sim) {
        simulationRunnable = sim;
    }

    public static double simulationTime() {
        return simulationRunnable.simulationTime();
    }

    int count = 0;
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // if (vehicle instanceof AutoTopoVehicle) {
        // // needed to calculate actual absolute position
        // ((AutoTopoVehicle) vehicle).setRoadSegment(this);
        // ((AutoTopoVehicle) vehicle).setSimulationTime(simulationTime);
        // }

        // System.out.println(simulationTime % 5);
        // controller.manageTopo();
        if (++count * dt % interval < precision) {
            System.out.println("AutoTopo at " + simulationTime);
            // controller.aggregate();
            if (simulationTime >= autoTopoStart) {
                controller.manageTopo();
            }
            if (!controller.checkTopo()) {
                try {
                    System.in.read();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    public LaneChangeGoal getLaneChangeGoal(IVehicleAdapter me) {
        return controller.getLaneChangeGoal(me);
    }

}
