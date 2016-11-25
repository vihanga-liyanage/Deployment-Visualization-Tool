var common = {
    isSource:true,
    isTarget:true,
    connector:"Straight",
    endpoint:"Dot",
    paintStyle:{ fillStyle:"lightblue", outlineWidth:1 },
    hoverPaintStyle:{ fillStyle:"lightblue" },
    connectorStyle:{ strokeStyle:"blue", lineWidth:2 },
    connectorHoverStyle:{ lineWidth:3 }
};

jsPlumb.ready(function() {
    //Enable dragging all item class elements
    jsPlumb.draggable($(".item"), {});

    // jsPlumb.connect({
    //     source:"db",
    //     target:"api-manager",
    //     endpoint: ["Dot", {
    //     	radius:10,
    //     	cssClass:"endPoint"
    //     }],
    //     connector: ["Straight"],
    //     anchor:[ "Perimeter", { shape:"Circle" } ],
    //     paintStyle:{ strokeStyle:"gray", lineWidth:5 },
 //        // endpointStyle:{ fillStyle:"lightgray", outlineColor:"gray" },
 //        overlays:[
 //            ["Arrow" , { width:20, length:20, location:0.97 }]
 //        ]
    // });

    jsPlumb.addEndpoint($(".item"), {
        endpoint:"Dot",
        anchor:[ "Perimeter", { shape:"Circle" } ],
        // anchors:[
        // 	["Bottom"],
        // 	["Top"],
        // 	["Left"],
        // 	["Right"]
        // ]
    }, common);

    jsPlumb.makeSource($(".item"), {
        endpoint:"Dot",
        maxConnections:3
    }, common);

    jsPlumb.makeTarget($(".item"), {
        endpoint:"Dot",
        anchor:[ "Perimeter", { shape:"Circle" } ],
        maxConnections:3
    }, common);

});


//--------------------------- Add Elements

$( document ).ready(function() {
  // Handler for .ready() called.
  $("#diagramContainer").append("<div id='dbs' class='item'> <img src='img/db.png'> </div>")
  alert("fdaf")

  var Div = $('<div>', { id: "X12" },
                                 { class: 'window ui-draggable' })
                      .css(
                                 { height: '100px',
                                   width: '100px',
                                   border: 'solid 1px'
                                 }
                          ).appendTo('body');
            jsPlumb.addEndpoint($(Div), targetEndpoint);
            jsPlumb.addEndpoint($(Div), sourceEndpoint);
            jsPlumb.draggable($(Div));
            $(Div).addClass('window');
});
