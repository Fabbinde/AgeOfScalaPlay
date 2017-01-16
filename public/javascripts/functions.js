var _data = {};
var currentBuilding = "";

/*
 * When the user clicks on the button, toggle between hiding and showing the
 * dropdown content
 */
function dropdownMenu() {
	document.getElementById("dropDownMenu").classList.toggle("show");
}

// Close the dropdown menu if the user clicks outside of it
window.onclick = function(event) {
	if (!event.target.matches('.dropbtn')) {

		var dropdowns = document.getElementsByClassName("dropdown_content");
		var i;
		for (i = 0; i < dropdowns.length; i++) {
			var openDropdown = dropdowns[i];
			if (openDropdown.classList.contains('show')) {
				openDropdown.classList.remove('show');
			}
		}
	}
}
window.setInterval(function refresh() {
	$.post("/data",

	function(data, status, obj) {

		_data = data;
		// console.log(data);

		$("#wood").html(data.wood);
		$("#food").html(data.food);
		$("#stone").html(data.stone);
		$("#gold").html(data.gold);
		$("#settler").html(data.settler);
		$("#gameName").html(data.gameName);
		$("#gameTime").html(data.gameTime);
		$("#score").html(data.score);
		$("#production").html(buildRessources("", data.production));
		$("#runningCosts").html(buildRessources("", data.runningCosts));

		checkBuildableBuildings(data.buildableBuildings.factory);

	});
}, 1000);

function checkBuildableBuildings(buildings) {
	buildings.forEach(function(b) {
		var icon = $("#" + b.name);

		if (b.kannGebautWerden == "ok") {
			icon.css("opacity", "1");
			icon.css("cursor", "pointer");
		} else {
			icon.css("opacity", "0.3");
			icon.css("cursor", "default");
		}
	})

}

function buildRessources(header, ress) {
	var BUFFER = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
	var a = "</br>"
	if (ress.wood != "0") {
		a += BUFFER + "Holz: " + ress.wood + "</br>";
	}
	if (ress.stone != "0") {
		a += BUFFER + "Stein: " + ress.stone + "</br>";
	}
	if (ress.gold != "0") {
		a += BUFFER + "Gold: " + ress.gold + "</br>";
	}
	if (ress.food != "0") {
		a += BUFFER + "Nahrung: " + ress.food + "</br>";
	}
	if (ress.settler != "0") {
		a += BUFFER + "Siedler: " + ress.settler + "</br>";
	}

	return header + a;
}

function closeBuildingInfo() {
	$(".building_info").css("visibility", "hidden");
}

function openBuildingInfo(name) {
	_data.buildableBuildings.factory.forEach(function(b) {
		if (name == b.name) {
			currentBuilding = b.name;
			// alle infos rausholen
			// $("#"+b.name)

			if (b.input) {
				var betriebskosten = buildRessources(
						"<span>Betriebskosten:</span>", b.input);
			} else {
				var betriebskosten = "";
			}

			if (b.output) {
				var produziert = buildRessources("<span>Produziert:</span>",
						b.output);
			} else {
				var produziert = "";
			}

			if (b.kapazitaet) {
				var kapa = "Kapazität: " + b.kapazitaet;
			} else {
				var kapa = "";
			}

			if (b.platz) {
				var platz = "Platz für " + b.platz + " Siedler";
			} else {
				var platz = "";
			}

			var text = "";
			text += '</br><h3 style="display: inline;">' + b._name + " ("
					+ b.anzahl + ")</h3></br>";
			text += '<span style="font-size: 13px;">' + b.information
					+ "</span></br>";
			text += '<span>'
					+ buildRessources("<span>Kosten:</span>",
							b.benoetigteBauRessourcen) + "</span>";
			text += '<span>' + betriebskosten || "" + "</span>";
			text += '<span>' + kapa || "" + '</span>';
			text += '<span>' + platz || "" + '</span>';
			text += '<span>' + produziert || "" + '</span>';

			$("#buldingInfoBody").html(text);
			$(".building_info").css("visibility", "visible");

			if (b.kannGebautWerden == "ok") {
				$("#buldingButton").css("opacity", "1");
				$("#buldingButton").css("cursor", "pointer");
			} else {
				$("#buldingButton").css("opacity", "0.3");
				$("#buldingButton").css("cursor", "default");
			}

		}
	});
}

function getDialogText(data) {
	var text = "";

	switch (data) {
	case "ok":
		text = "Erledigt!";
		break;
	case "fehler":
		text = "Hat nicht geklappt!";
		break;
	case "gebauedeFehlt":
		text = "Es sind nicht alle Gebäudevoraussetzungen vorhanden!";
		break;
	case "gebauedeEntfernt":
		text = "Gebäude erfolgreich abgerissen";
		break;
	case "gebauedeErstellt":
		text = "Gebäude wurde errichtet";
		break;
	case "gebauedeNichtVorhanden":
		text = "Gebäude ist nicht vorhanden";
		break;
	case "zuWenigRessourcen":
		text = "Zu wenig Ressourcen";

		break;

	default:
		text = "?";
		break;
	}
	return text;
}

function buildBuilding() {
	$.get("/baueGebauede/" + currentBuilding, function(data, status, obj) {
		console.log(data); // TODO HIER Modal oder so aufklappen lassen
		var text = getDialogText(data);
		$("#dialogP").html(text);
		$("#dialog").dialog();
		$("#dialog").dialog("option", "width", 300);

	});
}

function newGame() {
	$.get("/neuesSpiel", function(data, status, obj) {
		console.log(data); // TODO HIER Modal oder so aufklappen lassen
		$("#dialogP").html(data);
		$("#dialog").dialog();
		$("#dialog").dialog("option", "width", 300);
	});
}

function saveGame() {
	$.get("/spielSpeichern", function(data, status, obj) {
		$("#dialogP").html(data);
		$("#dialog").dialog();
		$("#dialog").dialog("option", "width", 300);
		console.log(data); // TODO HIER Modal oder so aufklappen lassen
	});
}

function loadGame() {
	$.get("/spielLaden", function(data, status, obj) {
		$("#dialogP").html(data);
		$("#dialog").dialog();
		$("#dialog").dialog("option", "width", 300);
		console.log(data); // TODO HIER Modal oder so aufklappen lassen
	});
}

function openHighscore() {
	$.get("/highscore", function(data, status, obj) {
		var text = "";
		data.forEach(function(line) {
			text += line + "</br>";
		})
		$("#dialogP").html(text);
		$("#dialog").dialog();
		$("#dialog").dialog("option", "width", 700);
		console.log(data); // TODO HIER Modal oder so aufklappen lassen
	});
}

function exit() {
	window.close();
}
