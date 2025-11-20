module bluezone.driver.forparkingcars.test {

	// DEPENDS ON
	requires bluezone.hexagon;
	requires io.github.proj.lib.portsadapters;
	requires io.cucumber.java;
	requires io.cucumber.datatable;
	requires io.cucumber.core;
	requires org.hamcrest;

	// PUBLISHES
	exports io.github.proj.bluezone.driver.forparkingcars.test;
	opens	io.github.proj.bluezone.driver.forparkingcars.test.stepdefs
	to		io.cucumber.java,
			picocontainer;
	
}
