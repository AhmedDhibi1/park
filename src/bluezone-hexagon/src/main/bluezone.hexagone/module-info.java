
module bluezone.hexagon {
	
	// DEPENDS ON
	// a tool to avoid boilerplate code writing
	requires static lombok;
	// utils of Java language
	requires io.github.proj.lib.javalangutils;

	// PUBLISHES
	// driving ports
	exports io.github.proj.bluezone.hexagon.ports.driving.forparkingcars;
	exports io.github.proj.bluezone.hexagon.ports.driving.forcheckingcars;
	exports io.github.proj.bluezone.hexagon.ports.driving.forconfiguringapp;
	// driven ports
	exports io.github.proj.bluezone.hexagon.ports.driven.forobtainingrates;
	exports io.github.proj.bluezone.hexagon.ports.driven.forstoringtickets;
	exports io.github.proj.bluezone.hexagon.ports.driven.forpaying;

	exports io.github.proj.bluezone.hexagon.factory;
}
