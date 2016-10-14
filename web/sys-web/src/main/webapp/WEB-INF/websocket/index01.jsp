<!DOCTYPE html>
<html>
<head>
    <title>WebSocket demo</title>
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no"/>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <style type="text/css"> 
        #connect-container {
            width: 100%;
        }

        #connect-container div {
            padding: 5px;
        }

        #console-container {
            width: 100%;
        }

        #console {
            border: 1px solid #CCCCCC;
            border-right-color: #999999;
            border-bottom-color: #999999;
            height: 170px;
            overflow-y: scroll;
            padding: 5px;
            width: 100%;
        }

        #console p {
            padding: 0;
            margin: 0;
        }
    </style>

    <script src="http://cdn.jsdelivr.net/sockjs/1/sockjs.min.js"></script>

    <script type="text/javascript">
        var ws = null;
        var url = null;
        var transports = [];
        var userId;

        function setConnected(connected) {
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            document.getElementById('echo').disabled = !connected;
            document.getElementById("makpairs").disabled = !connected;
        }

        function connect() {
        	 
            url = 'http://192.168.4.188:8094/sys/sysMsg/sockjs/webSocketServer?access_token=00000002IU6824TL9E8CB2FF03679795FE7EFE8F0A25BD35';
             alert(url)
            //alert("url:" + url);
            if (!url) {
                alert('Select whether to use W3C WebSocket or SockJS');
                return;
            }
            if (url.indexOf("sockjs")!=-1){
            	alert("sockjs01")
                //ws = new SockJS(url);
			var sock = new SockJS(url);
			alert(sock)
            }else {
                if ('WebSocket' in window) {
                	alert("WebSocket");
                    ws = new WebSocket(url);//ws://localhost:8080/myHandler
                } else {
                	alert("SockJS02")
                    ws = new SockJS(url);
                }
            }
            
			sock.onopen = function() {
				alert("onopen")
				console.log('open');
			};
			sock.onmessage = function(e) {
				alert("onmessage")
				alert(e.data);
				console.log('message', e.data);
			};
			sock.onclose = function() {
				alert("onclose")
				console.log('close');
			};


 
            ws.onopen = function () {
            	alert("onopen")
                setConnected(true);
                log('Info: connection opened.');
            };
            ws.onmessage = function (event) {
                log('Received: ' + event.data);
            };
            ws.onclose = function (event) {
            	alert("onclose")
                setConnected(false);
                log('Info: connection closed.');
                log(JSON.stringify(event));
            }; 
        }

        function disconnect() {
            if (ws != null) {
                ws.close();
                ws = null;
            }
            setConnected(false);
        }

        function echo() {
            if (ws != null) {
                var message = document.getElementById('message').value;
                log('Sent: ' + message);
                ws.send(message);
            } else {
                alert('connection not established, please connect.');
            }
        }
        function makpairs(){

        }

        function updateUrl(urlPath) {
        	userId = document.getElementById('pairMsg').value;
            if (urlPath.indexOf("/sockjs")!=-1){
                //url = 'http://'+ window.location.host + urlPath+ "?userId="+userId;
            	url = 'http://'+window.location.host+'/sys'+urlPath+'?access_token=00000002IU6824TL9E8CB2FF03679795FE7EFE8F0A25BD35';
            }else{
                if (window.location.protocol == 'http:') {
                    //url = 'ws://' + window.location.host + urlPath+ "?userId="+userId;
                	url = 'ws://'+window.location.host+'/sys'+urlPath+'?access_token=00000002IU6824TL9E8CB2FF03679795FE7EFE8F0A25BD35';
                } else {
                    //url = 'wss://' + window.location.host + urlPath+ "?userId="+userId;
                	url = 'wss://' + window.location.host + urlPath+ "?access_token=00000002IU6824TL9E8CB2FF03679795FE7EFE8F0A25BD35";
                }
            }
        }

        function updateTransport(transport) {
            alert(transport);
            transports = (transport == 'all') ? [] : [transport];
        }

        function log(message) {
            var console = document.getElementById('console');
            var p = document.createElement('p');
            p.style.wordWrap = 'break-word';
            p.appendChild(document.createTextNode(message));
            console.appendChild(p);
            while (console.childNodes.length > 25) {
                console.removeChild(console.firstChild);
            }
            console.scrollTop = console.scrollHeight;
        }
        var chars = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];

        function generateMixed(n) {
            var res = "";
            for(var i = 0; i < n ; i ++) {
                var id = Math.ceil(Math.random()*35);
                res += chars[id];
            }
            return res;
        }
    </script>
</head>
<body>
<noscript><h2 style="color: #ff0000">Seems your browser doesn't support Javascript! Websockets
    rely on Javascript being enabled. Please enable
    Javascript and reload this page!</h2></noscript>
<div>
    <label>这个是PAD端</label>
    <div id="connect-container">
        <input id="radio1" type="radio" name="group1" onclick="updateUrl('/sysMsg/webSocketServer');">
        <label for="radio1">W3C WebSocket</label>
        <br>
        <input id="radio2" type="radio" name="group1" onclick="updateUrl('/sysMsg/sockjs/webSocketServer');">
        <!-- 使用Java程序配置websocket时,看配置路径,看spring配置时,看配置文件 -->
        <!-- <input id="radio2" type="radio" name="group1" onclick="updateUrl('/sysMsg/webSocketServer');"> -->
        <label for="radio2">SockJS</label>

        <div id="sockJsTransportSelect" style="visibility:hidden;">
            <span>SockJS transport:</span>
            <select onchange="updateTransport(this.value)">
                <option value="all">all</option>
                <option value="websocket">websocket</option>
                <option value="xhr-polling">xhr-polling</option>
                <option value="jsonp-polling">jsonp-polling</option>
                <option value="xhr-streaming">xhr-streaming</option>
                <option value="iframe-eventsource">iframe-eventsource</option>
                <option value="iframe-htmlfile">iframe-htmlfile</option>
            </select>
        </div>
        <div>
            <button id="connect" onclick="connect();">Connect</button>
            <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button>
        </div>
        <div>
            <input type="text" id="pairMsg" style="width: 350px" name="pairMsg" value="1664"/>
        </div>
        <div>
            <button id="makpairs" onclick="makpairs();" disabled="disabled">配对</button>
        </div>
        <div>
            <textarea id="message" style="width: 350px">Here is a message!</textarea>
        </div>
        <div>
            <button id="echo" onclick="echo();" disabled="disabled">Echo message</button>
        </div>
    </div>
    <div id="console-container">
        <div id="console"></div>
    </div>
</div>
</body>
</html>