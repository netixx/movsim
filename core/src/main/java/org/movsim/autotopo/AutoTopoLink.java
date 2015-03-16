package org.movsim.autotopo;

import java.io.IOException;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.movsim.simulator.vehicles.Vehicle;

import fr.netixx.AutoTopo.adapters.IVehicleAdapter;
import fr.netixx.AutoTopo.adapters.impl.movsim.AutoTopoMovsimController;
import fr.netixx.AutoTopo.notifications.goals.AccelerationGoal;
import fr.netixx.AutoTopo.notifications.goals.LaneChangeGoal;
import fr.netixx.AutoTopo.notifications.goals.SpeedGoal;

public class AutoTopoLink implements SimulationTimeStep {

    private final double precision = 0.00001;
    // private final double interval = 0.5;
    private static final int dtInterval = 10;

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

    private SimulationRunnable simulationRunnable;

    public void setSimulationRunnable(SimulationRunnable sim) {
        simulationRunnable = sim;
    }

    public double simulationTime() {
        return simulationRunnable.simulationTime();
    }

    int count = 0;
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        controller.timeStep(simulationTime);
        if (++count % dtInterval < precision) {
            // System.out.println("AutoTopo at " + simulationTime);
            // controller.aggregate();
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

    public SpeedGoal getSpeedGoal(IVehicleAdapter vh) {
        return controller.getSpeedGoal(vh);
    }

    public AccelerationGoal getAccelerationGoal(IVehicleAdapter vh) {
        return controller.getAccelerationGoal(vh);
    }

    private Routing routing;

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public Route getRouteByName(String routeName) {
        if (routing.hasRoute(routeName)) {
            return routing.get(routeName);
        }
        return null;
    }

    public void writeStats() {
        controller.writeStats();

    }

}
