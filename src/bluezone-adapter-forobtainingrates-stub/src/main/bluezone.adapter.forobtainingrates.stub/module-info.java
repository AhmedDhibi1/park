import io.github.proj.bluezone.hexagon.ports.driven.forobtainingrates.ForObtainingRates;

module bluezone.adapter.forobtainingrates.stub {

	// DEPENDS ON
	requires bluezone.hexagon;
	requires io.github.proj.lib.portsadapters;

	// PUBLISHES
	exports io.github.proj.bluezone.adapter.forobtainingrates.stub;

	// SERVICES
	provides ForObtainingRates
		with io.github.proj.bluezone.adapter.forobtainingrates.stub.StubRateProviderAdapter;

}
