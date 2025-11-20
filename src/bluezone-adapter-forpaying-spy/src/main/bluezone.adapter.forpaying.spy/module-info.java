import io.github.proj.bluezone.adapter.forpaying.spy.SpyPaymentServiceAdapter;
import io.github.proj.bluezone.hexagon.ports.driven.forpaying.ForPaying;

module bluezone.adapter.forpaying.spy {

	// DEPENDS ON
	requires bluezone.hexagon;
	requires io.github.proj.lib.portsadapters;

	// PUBLISHES
	exports io.github.proj.bluezone.adapter.forpaying.spy;

	// SERVICES
	provides ForPaying
		with SpyPaymentServiceAdapter;
}
