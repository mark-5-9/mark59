package com.mark59.servermetrics.v2legacy;

public class ServerMetricsUtilsV2Legacy {

	

	@Deprecated
	public static ServerMetricsDriverInterface<ServerMetricsDriverConfig> createAndInitDriver(ServerMetricsDriverConfig config) {
		
		ServerMetricsDriverInterface<ServerMetricsDriverConfig> driver;
		
		if (AppConstantsServerMetricsV2Legacy.WINDOWS.equalsIgnoreCase(config.getOperatingSystem())){
			driver = new DosServerMetricsDriver();
		} else if (	AppConstantsServerMetricsV2Legacy.UNIX.equalsIgnoreCase(config.getOperatingSystem()) ||
				    AppConstantsServerMetricsV2Legacy.LINUX.equalsIgnoreCase(config.getOperatingSystem()))	{
			driver = new UnixServerMetricsDriver();
		} else {
			throw new IllegalArgumentException("Driver for OS " + config.getOperatingSystem() + " is Undefined. Supported OS's are:  UNIX  LINUX  WINDOWS");
		}

		driver.init(config);
		
		return driver;
	}


}
