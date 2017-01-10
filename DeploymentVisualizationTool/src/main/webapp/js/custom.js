
    var modelStr = '{"services":[';
    var serviceArray = [];

    var serviceNode = function ()
    {
        this.id = 0;
        this.type = "";
        this.name = "";
        this.image = "";
        this.ports = [];
        this.links = [];
        this.profile = "";
    };

    //Show a dialog box to select product profiles.
    var showAddProfile = function (state, evt, product)
    {
        var html = "";
        if (product == "wso2am") {
            html =
                '<div style="font-size: 20px;align-content: flex-end;text-align: left;">' +
                '<input type="checkbox" style="margin-right: 5px;" id="check1" value="publisher"/>publisher </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check2" value="store"/>store </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check3" value="keymanager"/>keymanager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check4" value="traffic-manager"/>traffic-manager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check5" value="gateway-manager"/>gateway-manager </br>' +
                '<input type="checkbox" style="margin-right: 5px;" id="check6" value="gateway-worker"/>gateway-worker </br>' +
                '</div>' +
                '<div style="margin-top: 15px; font-weight: bold;"></div>';
        }

        $('<div></div>').appendTo('body')
            .html(html)
            .dialog({
                modal: true,
                title: 'Choose profiles', zIndex: 10000, autoOpen: true,
                width: '250px', resizable: false,
                position: { my: 'left top', at: 'left+' + evt.clientX + ' top+' + evt.clientY},
                buttons: {
                    Ok: function () {
                        addProfileToService(state);
                        $(this).dialog("close");
                    }
                },
                close: function (event, ui) {
                    $(this).remove();
                }
            });
    };

    var addProfileToService = function (state)
    {
        //Get the checked profiles
        var str = "";
        if (document.getElementById("check1").checked) {
            str += '/' + document.getElementById("check1").value;
        }
        if (document.getElementById("check2").checked) {
            str += '/' + document.getElementById("check2").value;
        }
        if (document.getElementById("check3").checked) {
            str += '/' + document.getElementById("check3").value;
        }
        if (document.getElementById("check4").checked) {
            str += '/' + document.getElementById("check4").value;
        }
        if (document.getElementById("check5").checked) {
            str += '/' + document.getElementById("check5").value;
        }
        if (document.getElementById("check6").checked) {
            str += '/' + document.getElementById("check6").value;
        }

        //Add profiles to the label of the vertex and refresh
        var value = state.cell.value;
        value.setAttribute("label", str.substring(1));
        state.view.refresh();

    };

    var getModelFromDocker = function(baseDir, ymlFile, callback)
    {
        var ymlText = readFile(ymlFile);
        var data = jsyaml.load(ymlText);

        var serviceID = 1;
        for (var key in data.services)
        {
            processService(serviceID, key, data.services[key], baseDir);
            serviceID ++;
        }
        modelStr = modelStr.slice(0, -1) + ']}';
        callback(JSON.parse(modelStr));
    };

    var processService = function (serviceID, key, service, baseDir)
    {
        var node = new serviceNode();
        node.id = serviceID;
        modelStr += '{"id":' + serviceID + ',';

        //Extracting type and name from key
        var dashIndex = key.indexOf('-');
        var type = key.substring(0, dashIndex);
        var name = key.substring(dashIndex + 1);
        node.type = type;
        node.name = name;
        modelStr += '"type":"' + type + '",';
        modelStr += '"name":"' + name + '",';

        //set image
        node.image = service.image;
        modelStr += '"image":"' + service.image + '",';

        //set ports
        modelStr += '"ports":[';
        if (service.ports != null)
        {
            var ports = service.ports;
            for (p in ports)
            {
                node.ports.push(ports[p]);
                modelStr += '"' + ports[p] + '",';
            }
            modelStr = modelStr.slice(0, -1);
        }

        //set links
        modelStr += '],"links":[';

        if (service.build != null)
        {
            var dockerfile = baseDir + service.build.dockerfile;
            var dockerFileContent = readFile(dockerfile);

            console.log(key);
            console.log(dockerFileContent);

            var lines = dockerFileContent.split('\n');
            for (i in lines)
            {
                var parts = lines[i].split(' ');
                if (parts[0] == 'COPY') {
                    // console.log(parts[0] + ":" + parts[1]);
                }
            }
            // console.log(lines);
        }

        node.profile = name;
        modelStr += '],"profile":"' + name + '"},';

        console.log(node);
        serviceArray.push(node);
    };

    var readFile = function (file)
    {
        var allText = "Error";
        var rawFile = new XMLHttpRequest();
        rawFile.open("GET", file, false);
        rawFile.onreadystatechange = function ()
        {
            if(rawFile.readyState === 4)
            {
                if(rawFile.status === 200 || rawFile.status == 0)
                {
                    allText = rawFile.responseText;
                }
            }
        };
        rawFile.send(null);
        return allText;
    }

    //generate the graph by reading the configuration xml
    var generateGraph = function (configXML, editor)
    {
        var graphNode = editor.graph.container;
        graphNode.style.display = '';

        var doc = mxUtils.parseXml(configXML);
        var dec = new mxCodec(doc);
        dec.decode(doc.documentElement, editor.graph.getModel());

        // Makes sure nothing is selected in IE
        if (mxClient.IS_IE)
        {
            mxUtils.clearSelection();
        }
    };

    //function to load the model json file
    function loadJSON(path, callback)
    {
        var xobj = new XMLHttpRequest();
        xobj.overrideMimeType("application/json");
        xobj.open('GET', path, true);
        xobj.onreadystatechange = function () {
            if (xobj.readyState == 4 && xobj.status == "200") {
                // Required use of an anonymous callback as .open will NOT return a value but simply returns undefined in asynchronous mode
                callback(xobj.responseText);
            }
        };
        xobj.send(null);
    }

    //call back end and let the user download the generated configuration directory
    var getConfigurations = function(editor)
    {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXML", {xml:xml}, function(data) {
            window.open(data, '_blank');
        });
    };
    
    var getConfigurationsAutoGenLinks = function(editor)
    {
        var enc = new mxCodec();
        var node = enc.encode(editor.graph.getModel());
        var xml = mxUtils.getPrettyXml(node);

        console.log(xml);

        $.post("GetConfigFromXMLAutoGenLinks", {xml:xml}, function(data) {
            window.open(data, '_blank');
        });
    };

    //Generate graph links
    var genLinks = function(editor)
    {

        // get the required knowledge to build the links
        var linksPath = "links.json";
        $.getJSON(linksPath, function(json) {

            // console.log(editor.graph);
            var cells = editor.graph.model.cells;
            var services = new Array(); //store services data -> name and id

            // Push service data into array
            for (i in cells) {
                var cell = cells[i];
                if (cell.vertex == 1) {
                    services.push([getName(cell), cell.id]);
                }
            }
            for (var i=0; i<services.length; i++) {
                //get possible links to each service from json knowledge base
                var possibleLinks = json[services[i][0]];
                if (possibleLinks != null) {
                    for (var j=0; j < services.length; j++) {

                        // draw connections if links should exists and not already their.
                        if (possibleLinks.indexOf(services[j][0]) != -1) {
                            if (!isConnected(cells[services[i][1]], cells[services[j][1]]))
                            {
                                editor.graph.connectionHandler.insertEdge(
                                    cells[1], //parent
                                    null, //id
                                    null, //value
                                    cells[services[i][1]], //source
                                    cells[services[j][1]], //target
                                    null //style
                                );
                            }
                        }
                    }
                    editor.graph.view.refresh();
                }
            }
        });
    };

    //Check if two services have a connection
    var isConnected = function (cell1, cell2)
    {
        if (cell1.getEdgeCount() == 0)
            return false;
        for (var i = 0; i < cell1.getEdgeCount(); i++)
        {
            source = cell1.getEdgeAt(i).source;
            target = cell1.getEdgeAt(i).target;
            if (source == cell2 || target == cell2)
                return true;
        }
        return false;
    };

    //Construct service name -> <product>[_<profile-1>][/<profile-2>]...
    var getName = function (cell)
    {
        var name = cell.style;
        if (cell.value.attributes[0].value != "")
        {
            name += "_" + cell.value.attributes[0].value;
        }
        return name;
    };