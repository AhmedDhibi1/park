package io.github.proj.bluezone.startup;

import io.github.proj.bluezone.adapter.forparkingcars.webui.ForParkingCarsWebUIDriver;
import io.github.proj.bluezone.driver.forcheckingcars.test.ForCheckingCarsTestDriver;
import io.github.proj.bluezone.driver.forparkingcars.test.ForParkingCarsTestDriver;
import io.github.proj.bluezone.hexagon.factory.BlueZoneApp;
import io.github.proj.bluezone.hexagon.ports.driven.forobtainingrates.ForObtainingRates;
import io.github.proj.bluezone.hexagon.ports.driven.forpaying.ForPaying;
import io.github.proj.bluezone.hexagon.ports.driven.forstoringtickets.ForStoringTickets;
import io.github.proj.bluezone.hexagon.ports.driving.forcheckingcars.ForCheckingCars;
import io.github.proj.bluezone.hexagon.ports.driving.forparkingcars.ForParkingCars;
import io.github.proj.lib.javalangutils.StringUtils;
import io.github.proj.lib.portsadapters.Adapter;
import io.github.proj.lib.portsadapters.Driver;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;


public class DependencyConfigurator {

    private final AdapterSelector adapterSelector;

    public DependencyConfigurator (AdapterSelector adapterSelector ) {
        this.adapterSelector = adapterSelector;
    }


    public <T> T lookupDrivenPort ( Class<T> drivenPortType ) {
        String adapterName = adapterSelector.adapterNameForPort(drivenPortType);
        if (StringUtils.isBlank(adapterName)) {
            return null;
        }
        List<Provider<T>> adapters = ServiceLoader.load(drivenPortType).stream().filter(p -> isDrivenAdapterOfName(p.type(), adapterName)).collect(Collectors.toList());
        if (adapters == null || adapters.isEmpty()) {
            throw new RuntimeException("No adapter with name '" + adapterName + "' found for driven port '" + drivenPortType.getSimpleName() + "'");
        }
        if (adapters.size() > 1) {
            throw new RuntimeException("Many adapters with name '" + adapterName + "' found for driven port '" + drivenPortType.getSimpleName() + "'");
        }
        return adapters.get(0).get();
    }

    private static <T> boolean isDrivenAdapterOfName(Class<? extends T> adapterType, String adapterName) {
        Adapter adapterAnnotation = adapterType.getAnnotation(Adapter.class);
        if ((adapterAnnotation == null) || (adapterAnnotation.name() == null)) {
            return false;
        }
        return StringUtils.equalsIgnoreCaseTrimmed ( adapterAnnotation.name(), adapterName );
    }

    public BlueZoneApp buildApplication(ForObtainingRates rateProvider, ForStoringTickets ticketStore, ForPaying paymentService) {
        return BlueZoneApp.getInstance(rateProvider,ticketStore,paymentService);
    }

    public Driver lookupDriver ( Class<?> driverPortType, BlueZoneApp blueZoneApp ) {
        String adapterName = adapterSelector.adapterNameForPort(driverPortType);
        if (driverPortType.getSimpleName().equals(ForParkingCars.class.getSimpleName())) {
            if (AdapterSelector.TEST_CASES.equals(adapterName)) {
                return new ForParkingCarsTestDriver(blueZoneApp.carParker(), blueZoneApp.appConfigurator());
            }
            if (AdapterSelector.WEB_UI.equals(adapterName)) {
                return new ForParkingCarsWebUIDriver(blueZoneApp.carParker());
            }
            throw new RuntimeException("No driver with name '" + adapterName + "' found for driver port '" + driverPortType.getSimpleName() + "'");
        }
        if (driverPortType.getSimpleName().equals(ForCheckingCars.class.getSimpleName())) {
            if (AdapterSelector.TEST_CASES.equals(adapterName)) {
                return new ForCheckingCarsTestDriver(blueZoneApp.carChecker(), blueZoneApp.appConfigurator());
            }
            throw new RuntimeException("No driver with name '" + adapterName + "' found for driver port '" + driverPortType.getSimpleName() + "'");
        }
        throw new RuntimeException("No driver found for driver port '" + driverPortType.getSimpleName() + "'");
    }

}
