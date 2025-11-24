package io.github.proj.bluezone.startup;

import io.github.proj.bluezone.hexagon.factory.BlueZoneApp;
import io.github.proj.bluezone.hexagon.ports.driven.forobtainingrates.Rate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BlueZoneInitializer {

    public static void init(BlueZoneApp eParkingMeter) {
        // rate provider
        List<Rate> initialRates = new ArrayList<Rate>();
        initialRates.add(new Rate("BLUE_ZONE", new BigDecimal("0.80")));
        initialRates.add(new Rate("ORANGE_ZONE", new BigDecimal("0.95")));
        initialRates.add(new Rate("GREEN_ZONE", new BigDecimal("1.20")));
        eParkingMeter.appConfigurator().initRateProviderWith(initialRates);
        // payment service
        eParkingMeter.appConfigurator().setPaymentErrorPercentage(10);
    }

}
