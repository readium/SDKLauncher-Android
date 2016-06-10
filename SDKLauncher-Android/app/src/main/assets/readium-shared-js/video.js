function addVideoListeners() {
	console.log("addVideoListeners");
	// array of the events we want to track
	var events = new Array("abort", "canplay", "canplaythrough", "durationchange", "emptied", "ended", "error",
	"loadeddata", "loadedmetadata", "loadstart", "pause", "play", "playing", "progress", "ratechange", "seeked",
	"seeking", "stalled", "suspend", "timeupdate", "volumechange", "waiting");

	var vids = document.getElementsByTagName('video');
	console.log("vids: "+vids.length);
	for (var i = 0; i < vids.length; i++) {
		console.log("vids[i]: "+vids[i]);
		// add event listeners to the video
		for (var e in events) {
			addEventListener(vids[i], events[e]);
		}
	}
	function addEventListener(vid, event) {
		vid.addEventListener(event, showEvent, false);
		function showEvent(e) {
			var msg = "";
			if (e.type == "durationchange") {
				msg = e.type + "[" + vid.duration + "]";
			} else if (e.type == "seeked") {
				msg = e.type + "[" + vid.currentTime + "]";
			} else if (e.type == "timeupdate") {
				// do nothing as there are a lot of these
			} else if (e.type == "volumechange") {
				msg = "volume " + (vid.muted ? "muted" : vid.volume);
			} else {
				msg = e.type;
			}
			if (msg != "") {
				console.log(msg);
			}
		}
	}
}
