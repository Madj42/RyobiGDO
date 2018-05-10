/*
* Author: Justin Dybedahl
* Ryobi GDO200 Device Handler
* v1.2
*/


preferences {    
	section("Internal Access"){
		input "email", "text", title: "Email Address",required: true
		input "pass","text", title: "Password",required:true
		input "apikey", "text", title: "API Key",required: true		
		input "doorid", "text", title: "Garage Door ID",required: true
		input "internal_ip", "text", title: "Internal IP", required: true
		input "internal_port", "text", title: "Internal Port (default is 3042)", required: true
	}
}




metadata {
	definition (name: "Ryobi Garage Door", namespace: "madj42", author: "Justin Dybedahl") {
		capability "Actuator"
			capability "Switch"
			capability "Sensor"
            capability "Polling"
            capability "Refresh"
            capability "battery"
	}

    attribute "switch", "string"
    attribute "switch2", "string"

    command "on"
    command "off"
    command "dooropen"
    command "doorclose"

	// simulator metadata
	simulator {
	}

		// UI tile definitions
	tiles {
  	    multiAttributeTile(name: "door", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch1", key: "PRIMARY_CONTROL") {
            attributeState "closed", label: 'Door Closed', action: "dooropen", icon: "st.Home.home2", backgroundColor: "#ffffff", nextState: "opening"
			attributeState "open", label: 'Door Open', action: "doorclose", icon: "st.Home.home2", backgroundColor: "#79b821", nextState: "closing"
            attributeState "closing", label:'Door Closing', action:"doorclose", icon:"st.Home.home2", backgroundColor:"#00a0dc", nextState:"closed"
			attributeState "opening", label:'Door Opening', action:"dooropen", icon:"st.Home.home2", backgroundColor:"#79b821", nextState:"open"
            }
        }
        standardTile("button2", "device.switch2", width: 1, height: 1, canChangeIcon: false) {
			state "off", label: 'Light Off', action: "switch.on", icon: "st.Lighting.light11", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Light On', action: "switch.off", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		}
         standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh", icon:"st.secondary.refresh"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "battery", label: 'Battery: ${currentValue}%'
        }
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: '', icon: "https://logo-png.com/logo/ryobi-logo.png"
        }
		main "door"
			details (["door","button","button2","refresh","battery","icon"])
            }
}

def poll() {
refresh()
}

def refresh() {
getStatus()
}

def parse(String description){
//log.debug "Parse called"
	def msg = parseLanMessage(description)
    if (msg.body.startsWith("status:")) {
    	def batstatus = msg.body.split(':')[3]
    	def doorstatus = msg.body.split(':')[2]
    	def lightstatus = msg.body.split(':')[1]
	if (batstatus == "255") {
		sendEvent(name: "battery", value: 0)
	} else if (batstatus == null) {
		sendEvent(name: "battery", value: 0)
	} else if (batstatus == 'NA') {
		sendEvent(name: "battery", value: 0)
	} else {
	sendEvent(name: "battery", value: batstatus)
	}
    	if (lightstatus == "false") {
        //log.debug "Light OFF"
        sendEvent(name: "switch2", value: "off")
   		} else if (lightstatus == "true") {
        //log.debug "Light ON"
        sendEvent(name: "switch2", value: "on")
        }
       	if (doorstatus == "0") {
        //log.debug "Door Closed"
        sendEvent(name: "switch1", value: "closed")
   		} else if (doorstatus == "1") {
        //log.debug "Door Open"
        sendEvent(name: "switch1", value: "open")
        } else if (doorstatus == "2") {
        //log.debug "Door Closing"
        sendEvent(name: "switch1", value: "closing")
        } else if (doorstatus == "3") {
        //log.debug "Door Opening"
        sendEvent(name: "switch1", value: "opening")
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
			sendEvent(name: "switch2", value: "on")
            getStatus()
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
			sendEvent(name: "switch2", value: "off")
            getStatus()
			log.debug "Turning light OFF"
	}
    
def dooropen() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=dooropen&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
            
			sendHubCommand(result)
			sendEvent(name: "switch1", value: "opening")
            getStatus()
            runIn(15,getStatus)
			log.debug "OPENING Garage Door" 
            }
            
def doorclose() {
def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/?name=doorclose&doorid=${doorid}&apikey=${apikey}&email=${email}&pass=${pass}",
				headers: [
				HOST: "${internal_ip}:${internal_port}"
				]
				)
           
			sendHubCommand(result)
			sendEvent(name: "switch1", value: "closing")
            runIn(5,getStatus)
            runIn(25,getStatus)
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

def updated() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
		state.updatedLastRanAt = now()
		log.debug "Executing 'updated()'"
    	runIn(3, "updateDeviceNetworkID")
	}
	else {
//		log.trace "updated(): Ran within last 5 seconds so aborting."
	}
}


def updateDeviceNetworkID() {
	log.debug "Executing 'updateDeviceNetworkID'"
    def iphex = convertIPtoHex(internal_ip).toUpperCase()
    def porthex = convertPortToHex(internal_port).toUpperCase()
	device.setDeviceNetworkId(iphex + ":" + porthex)
}