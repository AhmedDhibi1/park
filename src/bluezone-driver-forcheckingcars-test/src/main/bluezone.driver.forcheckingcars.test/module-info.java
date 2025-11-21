module bluezone.driver.forcheckingcars.test {

	// DEPENDS ON
	requires bluezone.hexagon;
	requires io.github.proj.lib.portsadapters;
	requires org.testng;
	requires org.hamcrest;

	// PUBLISHES
	exports io.github.proj.bluezone.driver.forcheckingcars.test;
	exports io.github.proj.bluezone.driver.forcheckingcars.test.illegallyparkedcar
	to org.testng;

}
