function isEmpty(obj){
		for(var prop in obj){
				if(obj.hasOwnProperty(prop))
						return false;
		}
		return true;
}

function statusUpdate(){
    $.ajax({
	url: "/commands/status",
	data: null,
	success: statusCallback,
	dataType: 'json'});
}

function secondsToHHMMSS (raw){
    var secs = (raw % 60);
    //integer div
    var mins = ((raw / 60) >> 0) % 60;
    var hours = (raw / 3600) >> 0;
    var out = "";
    if(hours != 0){
	out += hours + ":";
    }
    if(mins >= 10){
	out += mins
    }else{
	out += "0" + mins
    }
    out += ":"
    if(secs >= 10){
	out += secs;
    }else{
	out += "0" + secs
    }
    return out;
}

function updateTimeDisplay(curTime, maxTime){
		var posElt = $('input#status-position');
		var oldTime = posElt.val();
		if(Math.abs(oldTime - curTime) > 3){
			//If this would trigger an onChange event where it would try to seek,
			// ignore this update
			posElt[0].ignoreNextChange = true;
		}
    posElt.attr('max', maxTime).val(curTime).slider('refresh');
    var curTimeString = secondsToHHMMSS(curTime);
    var maxTimeString = secondsToHHMMSS(maxTime);
    $('span.status-time').text(curTimeString + "/" + maxTimeString);
}

function timeDisplayIntervalUpdate(){
    if($('div#status-state').text() == '[playing]'){
      var timeElt = $('input#status-position');
      var curTime = parseInt(timeElt.val());
      var maxTime = parseInt(timeElt.attr('max'));
      updateTimeDisplay(curTime + 1, maxTime);
    }
}

function timeSliderChanged(field){
		field.old = (typeof(field.recent) == undefined) ? 0 : field.recent;
		field.recent = field.value;
		var diff = Math.abs(field.recent - field.old);
		if(diff > 3){
			if(field.ignoreNextChange == true){
					field.ignoreNextChange = false;
					return;
			}
			var newTime = $('input#status-position').val();
			doSeek(newTime);
		}
}

function doSeek(newTime){
		//Because seeking causes the slider to update, we need to ignore the next change
		$('input#status-position')[0].ignoreNextChange = true;
		runCommand('seek', newTime);
}

function statusCallback(data, ajaxStatus, xhr){
    var status = data.status;
		if(isEmpty(status)){
				return;
		}
    var curTime = parseInt(status.time);
    var maxTime = parseInt(status.length);
    updateTimeDisplay(curTime, maxTime);
    var state = status.state;
    $('div#status-state').text('[' + state + ']');
    var title = status.title;
    $('div#status-file').text(title);
    //updating radio button:
    //$('input#loop-options-' + new_val + '[name=loop-options]').attr('checked', true);
    //$('input[name=loop-options]').checkboxradio('refresh');
}

function runCommand(command, arg){
    if(command == ""){
				return;
    }
    $.ajax({
				url: "/commands/" + command + ((arg) ? "/" + arg : ""),
				data: null,
				success: function(data, s, x){
						statusCallback(data, s, x);
						//go ahead and update the status, since the response from the vlc
						//  http server does the current status, not the new one
						statusUpdate();
				},
				dataType: 'json'});
}

$(document).ready(function(){
    statusUpdate();
    setInterval(statusUpdate, 5000);
    setInterval(timeDisplayIntervalUpdate, 1000);
});