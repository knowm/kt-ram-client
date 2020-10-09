# kTPiServer (User) End-Points

This document details end-points available to system users ("User"). For end-points available to system administrators please see `AdminEndPoints.md` available in the `kt-ram-admin-client` package. 


## General Use

All interaction with the server takes place through calls to various endpoints (URLS), with information encoded into the urls path. Every interaction with the server will require the service endpoint, username and password. All traffic is encrypted by default via SSL by default, although this can be configured by the system admin. The path is as follows:  

	https://SERVER.knowm.ai:4242/kt-ram-server/ENDPOINT/parameter1/parameter2/etc

SERVER is the specific server you have been given access to. Configuration of this is the responsibility of the network administrator. ENDPOINT is the specific operation requested, which we list below. Parameters are (generally) passed to the specific end-point by appending parameters to the URL.


#### /get-module-info/{id}


Returns product information for the `Array Module` at the specified module port address. 

Example use:

	https://SERVER.knowm.ai:4242/kt-ram-server/get-module-info/2

Example JSON response:
	
	{
	"name": "Memristor Crossbar Module",
	"version": 1,
	"varient": "8x8x4 W+SDC",
	"dateOfManufacture": 1597721365192,
	"description": "Two crossbar die with access circuitry in a dual 32x32 array module.",
	"serialNumber": 12654230
	}	

Parameter `dateOfManufacture` is milliseconds from epoch. 
	
#### /get-driver-info

Example use:

	https://SERVER.knowm.ai:4242/kt-ram-server/get-driver-info

Example JSON response:
	
	{
	"name": "kT-RAM Server Pulse Driver",
	"version": 1,
	"varient": null,
	"dateOfManufacture": 1599501706982,
	"description": "Programmable pulse drivers and sense circuitry for kT-RAM half-core.",
	"serialNumber": 12636487
	}

Parameter `dateOfManufacture` is milliseconds from epoch. 
	
#### /get-board-info

Example use:

	https://SERVER.knowm.ai:4242/kt-ram-server/get-board-info

Example JSON response:

	{"name":"kT-RAM Server",
	"version":-1,
	"varient":null,
	"dateOfManufacture":1599501906982,
	"description":"Random access memristor research platform. Accepts memristor array and driver modules and exposes a web interface."
	,"serialNumber":12632640
	}
	
Parameter `dateOfManufacture` is milliseconds from epoch. 

#### /get-calibration-profile/{amplitude}/{width}/{seriesResistance}/{senseGain}

Example use:

	https://SERVER.knowm.ai:4242/kt-ram-server/get-calibration-profile/.15/500/40000/30
	
Example JSON response:

	{
	"readVoltage": 0.15,
	"readPulseWidth": 500,
	"gain": 30,
	"seriesResistor": 40000,
	"calibrationData": [
    [
      4.2636924,
      4.1973157,
      3.9503706,
      3.8320544,
      3.659424,
      3.3222263,
      3.288538,
      3.048343,
      2.818836,
      2.6793318,
      2.4423244,
      2.1799417,
      2.0459375,
      2.0171242,
      1.705927,
      1.2051617,
      0.9311535,
      0.68570846
    ],
    [
      6746.2256,
      7774.648,
      11500,
      13403.418,
      16320.001,
      23287.67,
      24000,
      30357.143,
      37168.14,
      42857.14,
      51000,
      64215.387,
      73684.21,
      75000,
      100000,
      179449.7,
      280000,
      499709.25
    ]
    ],
    "curveFitParameters": [
    189.55168,
    1334.8237,
    0.00037353332,
    1.7540486
    ]
    }

2D Vector `calibrationData` maps the measured sense voltage to the calibration module resistance. For example, a resistance of 6,746Ω resulted in a measured voltage of 4.2637 volts. The 1D vector `curveFitParameters` is given as `[a0,a1,a2,a3]`, which map to the following equation:

	R(v) = a0 * (a1 / v^a3 + a2)
	
, where `R(v)` is the resistance given the specified read voltage. The resistance, conductance or current returned by the kTPIServer will be a function of the given curve fit parameters and `R(v)` equation. If you wish to construct your own calibration curve or would like the raw voltage data, you can specify this when read-pulse measurements are requested. 


#### /set/{module-port}/{unit}/{array}/{columns}/{rows}

Used to add one or more devices to the active set, the set of devices that will be co-actived when a subsequent call to "read" or "write" is made.

Example use:

	https://SERVER.knowm.ai:4242/kt-ram-server/set/1/0/0/0/0
	

Example JSON response:

	true

Given the specified module port id, unit, array, column and row the server will add the device to the "active set". kTPiServer will respond with a boolean "true" if the operation was successful or will throw an error if the operation is not allowable. For both row and columns, multiple indicies may be provided in the same call. However, due to the constraints of each array module it may not be possible to active some devices with others at the same time. If this is the case, an error will be thrown when the "active set" is written to the array modules. For example, if we first set multiple rows:

	https://SERVER.knowm.ai:4242/kt-ram-server/set/1/0/0/0/0,1,2
	
The server will return:

	true

however, when we attempt to perform a read or write operation at a later time:

	https://SERVER.knowm.ai:4242/kt-ram-server/pulse-read/.15/500/40000/30/1/RESISTANCE

we will get the following error: 

	{
	"code": 500,
	"message": "javax.ws.rs.WebApplicationException: Only one active row is allowed from each set: [0-7], [8-15], [16-23], [24-32]"
	}

This restriction is related to the constraints of the specific array module. In this case, it is because column and row access is gated through 8:1 analog switches. Different array modules have different constraints, so is up to the user to identify the module and insure device access conforms to the specific array module constraints.


#### pulse-read/{amplitude}/{width}/{seriesResistance}/{senseGain}/{number}/{format}

Used to drive a read pulse across the active set of devices, using the specified read pulse profile.

| Property | Description |
|:--------:|:-----------:|
|amplitude | Read pulse voltage amplitude, in volts. 
|width|Read pulse width, in microseconds |
|seriesResistance| resistance of the series resistor, in ohms
|sensegain| gain factor of the sense circuit amplifier|
|number| number of pulses to apply|
|desired return format|RESISTANCE,CONDUCTANCE,CURRENT,VOLTAGE|

Example use:

First clear the active set:

	https://SERVER.knowm.ai:4242/kt-ram-server/clear
	
Second, set a device (add it to the active set):
	
	https://SERVER.knowm.ai:4242/kt-ram-server/set/1/0/0/0/0

Third, request five (5) read pulses be used and return the measured resistance: 

	https://SERVER.knowm.ai:4242/kt-ram-server/pulse-read/.15/500/40000/30/5/RESISTANCE

Example JSON response:

	[729557.56,736187.25,742460.6,735742.3,714447.94]

The above indicated that the device located at module port 1, unit 0, array 0, column 0, row 0 is approximatly 730kΩ 


#### pulse-read-activations/{amplitude}/{width}/{seriesResistance}/{senseGain}/{number}/{format}/{isPattern}

Used to drive read pulses, using the specified read pulse profile, across the devices specified by the posted activations. The activations are included as an HTTP post operation. An example can be found in the `KTRAMServerClient.java` class:

```
	public static List<float[]> pulseRead(float amplitude, float width, float seriesResistance, float senseGain,
			int numPulses, ReadFormat format, List<Activation> activations, boolean isPattern, String host) {

		String path = "pulse-read-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain
				+ "/" + numPulses + "/" + format + "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(new GenericType<List<float[]>>() {
			});
		}
	} 
```

The parameter `isPattern` controls how the posted activations are treated. If `isPattern=true`, the activations will be treated as co-activated devices, with the specified read pulses being drive across all devices at the same time. If `isPattern=false`, then the activations are treated as individual activations, with the specified read pulses being driven across each device in the list of activation serially, one after the other, in the order provided in the activation list. 


#### pulse-write/{amplitude}/{width}/{seriesResistance}/{number}


Used to drive a write pulses across the set of devices. The use of the word "write" in this case is to indicate that the operation is intended to change the state of the memristors and not to read its state. Positive applied voltages will (likely) increase device conductance, while negative applied voltages will (likely) decrease conductance, provided the device see a voltage that exceeds its adaptation thresholds. 

Example use:

	pulse-write/.5/250/30000/5

The above example will apply five (5) pulses of .5V magnitude and 250μs duration across the active set of devices, using a 30kΩ series resistor.

Example JSON response:

	true

A subsequent call to `pulse-read` will return the new device resistance. For example:

Clear the active set:

	https://SERVER.knowm.ai:4242/kt-ram-server/clear
	
Set a device (add it to the active set):
	
	https://SERVER.knowm.ai:4242/kt-ram-server/set/1/0/0/0/0

Perform a read operation to get initial state of the device: 

	https://SERVER.knowm.ai:4242/kt-ram-server/pulse-read/.15/500/40000/30/1/RESISTANCE

This returns:

	[704369.1]

Perform a write operation with a positive voltage:

	pulse-write/.65/250/30000/1

Perform a read operation to get the new state of the device: 

	https://SERVER.knowm.ai:4242/kt-ram-server/pulse-read/.15/500/40000/30/1/RESISTANCE
 
This returns:

	[370342.53]
	
Perform a write operation with a negative voltage:	
	
	pulse-write/-.5/250/30000/1
	
Perform a read operation to get the new state of the device: 

	https://SERVER.knowm.ai:4242/kt-ram-server/pulse-read/.15/500/40000/30/1/RESISTANCE	

This returns:

	[731316.4]
	
	
In the above example, we started with a device at 704kΩ. The application of a write pulse with positive voltage reduced the resistance to 370kΩ. A subsequent write pulse with negative voltage reset the device back to 731kΩ


#### pulse-write-activations/{amplitude}/{width}/{seriesResistance}/{number}/{isPattern}

Used to drive write pulses across the devices specified by the posted activations. The activations are included as an HTTP POST field. An example use can be found in the `KTRAMServerClient.java` class:

```
	public static boolean pulseWrite(float amplitude, float width, float seriesResistance, int numPulses,
			List<Activation> activations, boolean isPattern, String host) {

		String path = "pulse-write-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + numPulses
				+ "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(Boolean.class);
		}
	}
```

The parameter `isPattern` controls how the posted activations are treated. If `isPattern=true`, the activations will be treated as simultaneously co-activated devices, with the specified write pulses being driven across all devices at the same time. If `isPattern=false`, then the activations are treated as individual activations, with the specified write pulses being driven across each device in the list of activations serially, one after the other, in the order provided in the activation list. 


#### clear/


Used to clear the active set. That is, after a call to clear and before any new devices are set, no devices will be exposed to the pulse driver. Any subsequent call to `pulse-read` will return the measured open-circuit resistance. Note that this will change depending on the calibration profile!

Example use:

	https://SERVER.knowm.ai:4242/clear/
	
Example response:

	true
	
	
	
	