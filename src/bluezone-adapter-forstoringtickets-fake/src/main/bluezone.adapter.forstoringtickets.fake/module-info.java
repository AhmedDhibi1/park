import io.github.proj.bluezone.hexagon.ports.driven.forstoringtickets.ForStoringTickets;

module bluezone.adapter.forstoringtickets.fake {

	// DEPENDS ON
	requires bluezone.hexagon;
	requires io.github.proj.lib.portsadapters;

	// PUBLISHES
	exports io.github.proj.bluezone.adapter.forstoringtickets.fake;

	// SERVICES
	provides ForStoringTickets
	with io.github.proj.bluezone.adapter.forstoringtickets.fake.FakeTicketStoreAdapter;
	
}
