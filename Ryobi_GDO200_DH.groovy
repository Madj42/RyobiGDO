/*
* Author: Justin Dybedahl
* Ryobi GDO200/GDO201/GDO500 Device Handler
* v2.6
* Thanks to @Projectskydroid for the modifications.
*/

def clientVersion() {
    return "2.6"
}

preferences {    
	section("Configuration Parameters"){
		input "email", "email", title: "Email Address",required: true
		input "pass","password", title: "Password",required:true
		input "internal_ip", "text", title: "Internal IP", required: true
		input "internal_port", "text", title: "Internal Port (default is 3042)", required: true
		input title: "", description: "Ryobi GDO200 Device Handler v${clientVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph", required: true
        input title: "", description: "http://www.github.com/Madj42/RyobiGDO", displayDuringSetup: false, type: "paragraph", element: "paragraph"	
		input "pollInterval", "number", title: "Polling Interval", description: "Change polling frequency (in minutes)", defaultValue:4, range: "1..59", required: true, displayDuringSetup: true
    }
}




metadata {
	definition (name: "Ryobi Garage Door", namespace: "madj42", author: "Justin Dybedahl") {
		capability "Actuator"
		capability "Door Control"
		capability "Garage Door Control"
		capability "Switch"
		capability "Sensor"
		capability "Polling"
		capability "Momentary"
		capability "Relay Switch"
		capability "Refresh"
		capability "Battery"
	}

    attribute "switch", "string"

    command "on"
    command "off"
    command "open"
    command "close"

	// simulator metadata
	simulator {
	}

		// UI tile definitions
	tiles {
  	    multiAttributeTile(name: "door", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.door", key: "PRIMARY_CONTROL") {
            attributeState "closed", label: 'Door Closed', action: "door control.open", icon: "st.Home.home2", backgroundColor: "#ffffff"
			attributeState "open", label: 'Door Open', action: "door control.close", icon: "st.Home.home2", backgroundColor: "#79b821"
            attributeState "closing", label:'Door Closing', action:"door control.close", icon:"st.Home.home2", backgroundColor:"#00a0dc"
			attributeState "opening", label:'Door Opening', action:"door control.open", icon:"st.Home.home2", backgroundColor:"#79b821"
            }
        }
        standardTile("button2", "device.switch", width: 1, height: 1, canChangeIcon: false) {
			state "off", label: 'Light Off', action: "switch.on", icon: "st.Lighting.light11", backgroundColor: "#ffffff"
			state "on", label: 'Light On', action: "switch.off", icon: "st.Lighting.light11", backgroundColor: "#79b821"
		}
         standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh", icon:"st.secondary.refresh"
        }
        valueTile("battery", "device.Battery", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "Battery", label: 'Battery: ${currentValue}%'
        }
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: '', icon: "https://logo-png.com/logo/ryobi-logo.png"
		}
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.Home.home2"
        }
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.Home.home2"  
        }
		main "door"
			details (["door","button","button2","refresh","battery","icon","open","close"])
            }
}

def poll() {
getStatus()
}

def refresh() {
poll()
}

def updated() {
    	runIn(3, "updateDeviceNetworkID")
		schedule("0 0/${settings.pollInterval} * * * ?", poll)
}

def parse(String description){
//log.debug "Parse called"
	def msg = parseLanMessage(description)
    if (msg.body.startsWith("status:")) {
    	def batstatus = msg.body.split(':')[3]
    	def doorstatus = msg.body.split(':')[2]
    	def lightstatus = msg.body.split(':')[1]
	if (batstatus == "255") {
		sendEvent(name: "Battery", value: 0)
	} else if (batstatus == null) {
		sendEvent(name: "Battery", value: 0)
	} else if (batstatus == 'NA') {
		sendEvent(name: "Battery", value: 0)
	} else {
	sendEvent(name: "Battery", value: batstatus)
	}
    	if (lightstatus == "false") {
        //log.debug "Light OFF"
        sendEvent(name: "switch", value: "off")
   		} else if (lightstatus == "true") {
        //log.debug "Light ON"
        sendEvent(name: "switch", value: "on")
        }
       	if (doorstatus == "0") {
        //log.debug "Door Closed"
        sendEvent(name: "door", value: "closed")
   		} else if (doorstatus == "1") {
        //log.debug "Door Open"
        sendEvent(name: "door", value: "open")
        } else if (doorstatus == "2") {
        //log.debug "Door Closing"
        sendEvent(name: "door", value: "closing")
        } else if (doorstatus == "3") {
        //log.debug "Door Opening"
        sendEvent(name: "door", value: "opening")
        }
    }
}

def on() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=lighton&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
     
			sendHubCommand(result)
			sendEvent(name: "switch", value: "on")
            runIn(5,getStatus)
			log.debug "Turning light ON" 
            }

def off() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=lightoff&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
                
			sendHubCommand(result)
			sendEvent(name: "switch", value: "off")
           runIn(5,getStatus)
			log.debug "Turning light OFF"
	}
    
def open() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=dooropen&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
            
			sendHubCommand(result)
			sendEvent(name: "door", value: "opening")
            runIn(5,getStatus)
            runIn(17,getStatus)
			log.debug "OPENING Garage Door" 
            }
            
def close() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=doorclose&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
           
			sendHubCommand(result)
			sendEvent(name: "door", value: "closing")
            runIn(5,getStatus)
            runIn(21,getStatus)
			log.debug "CLOSING Garage Door" 
            }
 
def getStatus() {
	def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=status&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				])
			sendHubCommand(result)
			log.debug "Getting Status"
	}
    
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}

def updateDeviceNetworkID() {
	log.debug "Executing 'updateDeviceNetworkID'"
    def iphex = convertIPtoHex(internal_ip).toUpperCase()
    def porthex = convertPortToHex(internal_port).toUpperCase()
	device.setDeviceNetworkId(iphex + ":" + porthex)
}